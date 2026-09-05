package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SearchRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.entity.StudentEntity;
import com.odschool.enums.SearchStatus;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
import com.odschool.repository.StandardRepository;
import com.odschool.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    private static final int VALID_STANDARD_ID = 1;
    private static final int NON_EXISTENT_STANDARD_ID = 888;

    private static final int VALID_DIVISION_ID = 1;
    private static final int NON_EXISTENT_DIVISION_ID = 888;

    private static final int STANDARD_VALUE = 10;
    private static final char DIVISION_VALUE = 'A';

    private static final float DELTA = 0.0001f;

    @Mock
    private StandardRepository standardRepository;

    @Mock
    private DivisionRepository divisionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private ReportService reportService;

    // ==========================================================================================
    // Common / reusable helper methods
    //
    // Note: unlike the CRUD services, most tests here need their own bespoke dataset (different
    // pass/fail mixes, percentages, list sizes), so entities are built locally per test rather
    // than shared via @BeforeEach. Helper builders and assertion helpers still follow the same
    // pattern used across the other *ServiceTest classes.
    // ==========================================================================================

    private StandardEntity buildStandardEntity(int id, int standard) {
        StandardEntity entity = new StandardEntity();
        entity.setId(id);
        entity.setStandard(standard);
        return entity;
    }

    private DivisionEntity buildDivisionEntity(int id, char division) {
        DivisionEntity entity = new DivisionEntity();
        entity.setId(id);
        entity.setDivision(division);
        return entity;
    }

    private StudentEntity buildStudentEntity(int id, String studentName, double percentage, String result, DivisionEntity divisionEntity) {
        StudentEntity entity = new StudentEntity();
        entity.setId(id);
        entity.setStudentName(studentName);
        entity.setPercentage(percentage);
        entity.setResult(result);
        entity.setDivisionEntity(divisionEntity);
        return entity;
    }

    private StudentResponse buildStudentResponse(int id, String studentName, double percentage, String result, int divisionId) {
        StudentResponse response = new StudentResponse();
        response.setId(id);
        response.setStudentName(studentName);
        response.setPercentage(percentage);
        response.setResult(result);
        response.setDivisionId(divisionId);
        return response;
    }

    private SearchRequest buildSearchRequest(SearchStatus status, Integer standardId, Integer divisionId) {
        SearchRequest request = new SearchRequest();
        request.setSearchStatus(status);
        if (standardId != null) {
            request.setStandardId(standardId);
        }
        if (divisionId != null) {
            request.setDivisionId(divisionId);
        }
        return request;
    }

    private void assertSuccessfulApiResponse(ResponseEntity<Object> result, HttpStatus expectedHttpStatus) {
        assertNotNull(result, "ResponseEntity should not be null");
        assertEquals(expectedHttpStatus, result.getStatusCode());
        assertNotNull(result.getBody(), "ResponseEntity body should not be null");
        assertInstanceOf(ApiResponse.class, result.getBody());

        ApiResponse apiResponse = (ApiResponse) result.getBody();
        assertTrue(apiResponse.isSuccess(), "ApiResponse success flag should be true");
        assertEquals(HttpStatus.OK.value(), apiResponse.getHttpStatus());
    }

    private void assertApiExceptionThrown(Executable executable, String expectedMessage, HttpStatus expectedStatus) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(expectedStatus, exception.getHttpStatus());
    }

    private void assertNoRepositoryInteractions() {
        verifyNoInteractions(standardRepository);
        verifyNoInteractions(divisionRepository);
        verifyNoInteractions(studentRepository);
    }

    // ==========================================================================================
    // getTopThreeOfStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("getTopThreeOfStandard()")
    class GetTopThreeOfStandardTests {

        @Test
        @DisplayName("Should return only the single lowest-percentage passing student " +
                "(documents current sort-ascending + limit(1) behavior, despite the method name)")
        void getTopThreeOfStandard_Success_ReturnsLowestPercentagePassingStudent() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');
            DivisionEntity div2 = buildDivisionEntity(2, 'B');

            StudentEntity s1 = buildStudentEntity(1, "Alice", 95.0, "Pass", div1);
            StudentEntity s2 = buildStudentEntity(2, "Bob", 60.0, "PASS", div1);
            StudentEntity s3 = buildStudentEntity(3, "Cara", 99.0, "Fail", div2);
            StudentEntity s4 = buildStudentEntity(4, "Dan", 80.0, "pass", div2);

            StudentResponse r2 = buildStudentResponse(2, "Bob", 60.0, "PASS", 1);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1, div2));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1, 2))).thenReturn(List.of(s1, s2, s3, s4));
            when(mapInterface.toStudentResponse(s2)).thenReturn(r2);

            ResponseEntity<Object> result = reportService.getTopThreeOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(1, data.size(), "Current implementation limits to 1 result, not three");
            assertEquals(r2, data.get(0), "Ascending sort means the lowest passing percentage (Bob, 60.0) is returned");
            assertEquals("Top Three Student Of Standard: " + STANDARD_VALUE, apiResponse.getMessage());

            // Only the returned student should ever reach the mapper; the "better" passing students
            // (Alice, Dan) are silently dropped by the current sort/limit combination.
            verify(mapInterface, times(1)).toStudentResponse(s2);
            verify(mapInterface, never()).toStudentResponse(s1);
            verify(mapInterface, never()).toStudentResponse(s4);
        }

        @Test
        @DisplayName("Should return an empty list successfully when no student has a passing result")
        void getTopThreeOfStandard_Success_ReturnsEmptyList_WhenNoStudentsPass() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');
            StudentEntity s1 = buildStudentEntity(1, "Alice", 40.0, "Fail", div1);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(List.of(s1));

            ResponseEntity<Object> result = reportService.getTopThreeOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toStudentResponse(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the standard does not exist, without querying downstream repositories")
        void getTopThreeOfStandard_Failure_WhenStandardNotFound() {
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getTopThreeOfStandard(NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(divisionRepository);
            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("Should throw NullPointerException when a student's result field is null " +
                "(no null-check before equalsIgnoreCase; affects all six report methods)")
        void getTopThreeOfStandard_Failure_WhenStudentResultIsNull() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');
            StudentEntity studentWithNullResult = buildStudentEntity(1, "Alice", 70.0, null, div1);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(List.of(studentWithNullResult));

            assertThrows(NullPointerException.class, () -> reportService.getTopThreeOfStandard(VALID_STANDARD_ID));
        }
    }

    // ==========================================================================================
    // getTopThreeOfDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("getTopThreeOfDivision()")
    class GetTopThreeOfDivisionTests {

        @Test
        @DisplayName("Should return the two lowest-percentage passing students " +
                "(documents current sort-ascending + limit(2) behavior, despite the method name)")
        void getTopThreeOfDivision_Success_ReturnsTwoLowestPercentagePassingStudents() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);

            StudentEntity s1 = buildStudentEntity(1, "Alice", 95.0, "Pass", divisionEntity);
            StudentEntity s2 = buildStudentEntity(2, "Bob", 60.0, "PASS", divisionEntity);
            StudentEntity s3 = buildStudentEntity(3, "Cara", 99.0, "Fail", divisionEntity);
            StudentEntity s4 = buildStudentEntity(4, "Dan", 80.0, "pass", divisionEntity);

            StudentResponse r2 = buildStudentResponse(2, "Bob", 60.0, "PASS", VALID_DIVISION_ID);
            StudentResponse r4 = buildStudentResponse(4, "Dan", 80.0, "pass", VALID_DIVISION_ID);

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(List.of(s1, s2, s3, s4));
            when(mapInterface.toStudentResponse(s2)).thenReturn(r2);
            when(mapInterface.toStudentResponse(s4)).thenReturn(r4);

            ResponseEntity<Object> result = reportService.getTopThreeOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size(), "Current implementation limits to 2 results, not three");
            assertEquals(List.of(r2, r4), data);
            assertEquals("Top Three Student Of Division: " + DIVISION_VALUE, apiResponse.getMessage());

            verify(mapInterface, never()).toStudentResponse(s1);
        }

        @Test
        @DisplayName("Should return an empty list successfully when no student has a passing result")
        void getTopThreeOfDivision_Success_ReturnsEmptyList_WhenNoStudentsPass() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            StudentEntity s1 = buildStudentEntity(1, "Alice", 40.0, "Fail", divisionEntity);

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(List.of(s1));

            ResponseEntity<Object> result = reportService.getTopThreeOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the division does not exist, without querying the student repository")
        void getTopThreeOfDivision_Failure_WhenDivisionNotFound() {
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getTopThreeOfDivision(NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(studentRepository);
        }
    }

    // ==========================================================================================
    // getAveragePassedStudentsOfStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("getAveragePassedStudentsOfStandard()")
    class GetAveragePassedStudentsOfStandardTests {

        @Test
        @DisplayName("Should return the correct pass percentage when students exist")
        void getAveragePassedStudentsOfStandard_Success_ReturnsCorrectPercentage() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');

            List<StudentEntity> students = List.of(
                    buildStudentEntity(1, "Alice", 90.0, "Pass", div1),
                    buildStudentEntity(2, "Bob", 85.0, "PASS", div1),
                    buildStudentEntity(3, "Cara", 55.0, "pass", div1),
                    buildStudentEntity(4, "Dan", 30.0, "Fail", div1)
            );

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            float actual = (Float) apiResponse.getData();
            assertEquals(75.0f, actual, DELTA, "3 of 4 students passed => 75%");
            assertEquals("Average Passed Student Of Standard: " + STANDARD_VALUE, apiResponse.getMessage());
        }

        @Test
        @DisplayName("Should return zero percent when students exist but none passed")
        void getAveragePassedStudentsOfStandard_Success_ReturnsZero_WhenNoStudentsPassed() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');
            List<StudentEntity> students = List.of(buildStudentEntity(1, "Alice", 30.0, "Fail", div1));

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertEquals(0.0f, actual, DELTA);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the standard does not exist")
        void getAveragePassedStudentsOfStandard_Failure_WhenStandardNotFound() {
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getAveragePassedStudentsOfStandard(NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(divisionRepository);
            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("Edge case: should return NaN (not 0% or an error) when the standard has no students at all " +
                "(0f/0f division-by-zero is not currently guarded against)")
        void getAveragePassedStudentsOfStandard_EdgeCase_ReturnsNaN_WhenNoStudentsExist() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());
            when(studentRepository.findByDivisionEntityIdIn(List.of())).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertTrue(Float.isNaN(actual), "Dividing 0 passed by 0 total currently yields NaN rather than a handled 0% or error");
        }
    }

    // ==========================================================================================
    // getAverageFailedStudentsOfStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("getAverageFailedStudentsOfStandard()")
    class GetAverageFailedStudentsOfStandardTests {

        @Test
        @DisplayName("Should return the correct fail percentage when students exist")
        void getAverageFailedStudentsOfStandard_Success_ReturnsCorrectPercentage() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');

            List<StudentEntity> students = List.of(
                    buildStudentEntity(1, "Alice", 90.0, "Pass", div1),
                    buildStudentEntity(2, "Bob", 85.0, "PASS", div1),
                    buildStudentEntity(3, "Cara", 55.0, "pass", div1),
                    buildStudentEntity(4, "Dan", 30.0, "Fail", div1)
            );

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            float actual = (Float) apiResponse.getData();
            assertEquals(25.0f, actual, DELTA, "1 of 4 students failed => 25%");
            assertEquals("Average Failed Student Of Standard: " + STANDARD_VALUE, apiResponse.getMessage());
        }

        @Test
        @DisplayName("Should return zero percent when students exist but none failed")
        void getAverageFailedStudentsOfStandard_Success_ReturnsZero_WhenNoStudentsFailed() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            DivisionEntity div1 = buildDivisionEntity(1, 'A');
            List<StudentEntity> students = List.of(buildStudentEntity(1, "Alice", 90.0, "Pass", div1));

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(List.of(div1));
            when(studentRepository.findByDivisionEntityIdIn(List.of(1))).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertEquals(0.0f, actual, DELTA);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the standard does not exist")
        void getAverageFailedStudentsOfStandard_Failure_WhenStandardNotFound() {
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getAverageFailedStudentsOfStandard(NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(divisionRepository);
            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("Edge case: should return NaN when the standard has no students at all")
        void getAverageFailedStudentsOfStandard_EdgeCase_ReturnsNaN_WhenNoStudentsExist() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());
            when(studentRepository.findByDivisionEntityIdIn(List.of())).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertTrue(Float.isNaN(actual));
        }
    }

    // ==========================================================================================
    // getAveragePassedStudentsOfDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("getAveragePassedStudentsOfDivision()")
    class GetAveragePassedStudentsOfDivisionTests {

        @Test
        @DisplayName("Should return the correct pass percentage when students exist")
        void getAveragePassedStudentsOfDivision_Success_ReturnsCorrectPercentage() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);

            List<StudentEntity> students = List.of(
                    buildStudentEntity(1, "Alice", 90.0, "Pass", divisionEntity),
                    buildStudentEntity(2, "Bob", 85.0, "PASS", divisionEntity),
                    buildStudentEntity(3, "Cara", 55.0, "pass", divisionEntity),
                    buildStudentEntity(4, "Dan", 30.0, "Fail", divisionEntity)
            );

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            float actual = (Float) apiResponse.getData();
            assertEquals(75.0f, actual, DELTA);
            assertEquals("Average Passed Student Of Division: " + DIVISION_VALUE, apiResponse.getMessage());
        }

        @Test
        @DisplayName("Should return zero percent when students exist but none passed")
        void getAveragePassedStudentsOfDivision_Success_ReturnsZero_WhenNoStudentsPassed() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            List<StudentEntity> students = List.of(buildStudentEntity(1, "Alice", 30.0, "Fail", divisionEntity));

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertEquals(0.0f, actual, DELTA);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the division does not exist")
        void getAveragePassedStudentsOfDivision_Failure_WhenDivisionNotFound() {
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getAveragePassedStudentsOfDivision(NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("Edge case: should return NaN when the division has no students at all")
        void getAveragePassedStudentsOfDivision_EdgeCase_ReturnsNaN_WhenNoStudentsExist() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = reportService.getAveragePassedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertTrue(Float.isNaN(actual));
        }
    }

    // ==========================================================================================
    // getAverageFailedStudentsOfDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("getAverageFailedStudentsOfDivision()")
    class GetAverageFailedStudentsOfDivisionTests {

        @Test
        @DisplayName("Should return the correct fail percentage when students exist")
        void getAverageFailedStudentsOfDivision_Success_ReturnsCorrectPercentage() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);

            List<StudentEntity> students = List.of(
                    buildStudentEntity(1, "Alice", 90.0, "Pass", divisionEntity),
                    buildStudentEntity(2, "Bob", 85.0, "PASS", divisionEntity),
                    buildStudentEntity(3, "Cara", 55.0, "pass", divisionEntity),
                    buildStudentEntity(4, "Dan", 30.0, "Fail", divisionEntity)
            );

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            float actual = (Float) apiResponse.getData();
            assertEquals(25.0f, actual, DELTA);
            assertEquals("Average Failed Student Of Division: " + DIVISION_VALUE, apiResponse.getMessage());
        }

        @Test
        @DisplayName("Should return zero percent when students exist but none failed")
        void getAverageFailedStudentsOfDivision_Success_ReturnsZero_WhenNoStudentsFailed() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            List<StudentEntity> students = List.of(buildStudentEntity(1, "Alice", 90.0, "Pass", divisionEntity));

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(students);

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertEquals(0.0f, actual, DELTA);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the division does not exist")
        void getAverageFailedStudentsOfDivision_Failure_WhenDivisionNotFound() {
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> reportService.getAverageFailedStudentsOfDivision(NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verifyNoInteractions(studentRepository);
        }

        @Test
        @DisplayName("Edge case: should return NaN when the division has no students at all")
        void getAverageFailedStudentsOfDivision_EdgeCase_ReturnsNaN_WhenNoStudentsExist() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = reportService.getAverageFailedStudentsOfDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            float actual = (Float) ((ApiResponse) result.getBody()).getData();
            assertTrue(Float.isNaN(actual));
        }
    }

    // ==========================================================================================
    // getRepost() - dispatcher
    // ==========================================================================================

    @Nested
    @DisplayName("getRepost()")
    class GetRepostTests {

        @Test
        @DisplayName("Should delegate to getTopThreeOfStandard() when searchStatus is TopThreeStandard")
        void getRepost_DelegatesTo_TopThreeStandard() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());
            when(studentRepository.findByDivisionEntityIdIn(List.of())).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.TopThreeStandard, VALID_STANDARD_ID, null);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Top Three Student Of Standard: " + STANDARD_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should delegate to getTopThreeOfDivision() when searchStatus is TopThreeDivision")
        void getRepost_DelegatesTo_TopThreeDivision() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.TopThreeDivision, null, VALID_DIVISION_ID);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Top Three Student Of Division: " + DIVISION_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should delegate to getAveragePassedStudentsOfStandard() when searchStatus is AvgPassStudentStandard")
        void getRepost_DelegatesTo_AvgPassStudentStandard() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());
            when(studentRepository.findByDivisionEntityIdIn(List.of())).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.AvgPassStudentStandard, VALID_STANDARD_ID, null);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Average Passed Student Of Standard: " + STANDARD_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should delegate to getAveragePassedStudentsOfDivision() when searchStatus is AvgPassStudentDivision")
        void getRepost_DelegatesTo_AvgPassStudentDivision() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.AvgPassStudentDivision, null, VALID_DIVISION_ID);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Average Passed Student Of Division: " + DIVISION_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should delegate to getAverageFailedStudentsOfStandard() when searchStatus is AvgFailStudentStandard")
        void getRepost_DelegatesTo_AvgFailStudentStandard() {
            StandardEntity standardEntity = buildStandardEntity(VALID_STANDARD_ID, STANDARD_VALUE);
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());
            when(studentRepository.findByDivisionEntityIdIn(List.of())).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.AvgFailStudentStandard, VALID_STANDARD_ID, null);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Average Failed Student Of Standard: " + STANDARD_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should delegate to getAverageFailedStudentsOfDivision() when searchStatus is AvgFailStudentDivision")
        void getRepost_DelegatesTo_AvgFailStudentDivision() {
            DivisionEntity divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, DIVISION_VALUE);
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            SearchRequest request = buildSearchRequest(SearchStatus.AvgFailStudentDivision, null, VALID_DIVISION_ID);

            ResponseEntity<Object> result = reportService.getRepost(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("Average Failed Student Of Division: " + DIVISION_VALUE,
                    ((ApiResponse) result.getBody()).getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when searchStatus is null, without any repository interaction " +
                "(no null-guard before request.getSearchStatus().equals(...) is called)")
        void getRepost_Failure_WhenSearchStatusIsNull() {
            SearchRequest request = buildSearchRequest(null, VALID_STANDARD_ID, VALID_DIVISION_ID);

            assertThrows(NullPointerException.class, () -> reportService.getRepost(request));

            assertNoRepositoryInteractions();
        }
    }
}
