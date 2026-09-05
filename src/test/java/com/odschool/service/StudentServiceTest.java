package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.GetAllStudentRequest;
import com.odschool.dtos.StudentRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StudentEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
import com.odschool.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    private static final int VALID_DIVISION_ID = 1;
    private static final int NON_EXISTENT_DIVISION_ID = 888;
    private static final int NEGATIVE_DIVISION_ID = -1;

    private static final int VALID_STUDENT_ID = 10;
    private static final int NON_EXISTENT_STUDENT_ID = 999;
    private static final int NEGATIVE_STUDENT_ID = -1;

    private static final int VALID_SCHOOL_ID = 1;
    private static final int NEGATIVE_SCHOOL_ID = -1;

    private static final int VALID_STANDARD_ID = 1;
    private static final int NEGATIVE_STANDARD_ID = -1;

    private static final String VALID_STUDENT_NAME = "John Doe";
    private static final String UPDATED_STUDENT_NAME = "Jane Doe";
    private static final String BLANK_STUDENT_NAME = "   ";

    private static final double VALID_OBTAIN_MARKS = 450;
    private static final double NEGATIVE_OBTAIN_MARKS = -10;

    private static final double VALID_PERCENTAGE = 90.0;
    private static final double NEGATIVE_PERCENTAGE = -5.0;
    private static final double OVER_100_PERCENTAGE = 105.0;

    private static final String VALID_RESULT = "PASS";
    private static final String BLANK_RESULT = "   ";

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DivisionRepository divisionRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private StudentService studentService;

    private DivisionEntity divisionEntity;
    private StudentEntity studentEntity;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        divisionEntity = buildDivisionEntity(VALID_DIVISION_ID);
        studentEntity = buildStudentEntity(VALID_STUDENT_ID, VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, divisionEntity);
        studentResponse = buildStudentResponse(VALID_STUDENT_ID, VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);
    }

    // ==========================================================================================
    // Common / reusable helper methods
    // ==========================================================================================

    private DivisionEntity buildDivisionEntity(int id) {
        DivisionEntity entity = new DivisionEntity();
        entity.setId(id);
        return entity;
    }

    private StudentEntity buildStudentEntity(int id, String studentName, double obtainMarks, double percentage,
                                              String result, DivisionEntity divisionEntity) {
        StudentEntity entity = new StudentEntity();
        entity.setId(id);
        entity.setStudentName(studentName);
        entity.setObtainMarks(obtainMarks);
        entity.setPercentage(percentage);
        entity.setResult(result);
        entity.setDivisionEntity(divisionEntity);
        return entity;
    }

    private StudentResponse buildStudentResponse(int id, String studentName, double obtainMarks, double percentage,
                                                  String result, int divisionId) {
        StudentResponse response = new StudentResponse();
        response.setId(id);
        response.setStudentName(studentName);
        response.setObtainMarks(obtainMarks);
        response.setPercentage(percentage);
        response.setResult(result);
        response.setDivisionId(divisionId);
        return response;
    }

    private StudentRequest buildStudentRequest(String studentName, double obtainMarks, double percentage, String result) {
        StudentRequest request = new StudentRequest();
        request.setStudentName(studentName);
        request.setObtainMarks(obtainMarks);
        request.setPercentage(percentage);
        request.setResult(result);
        return request;
    }

    private GetAllStudentRequest buildGetAllStudentRequest(Integer schoolId, Integer divisionId, Integer standardId) {
        GetAllStudentRequest request = new GetAllStudentRequest();
        if (schoolId != null) {
            request.setSchoolId(schoolId);
        }
        if (divisionId != null) {
            request.setDivisionId(divisionId);
        }
        if (standardId != null) {
            request.setStandardId(standardId);
        }
        return request;
    }

    private void mockSuccessfulEntityToResponseMapping(StudentEntity entity, StudentResponse response) {
        when(mapInterface.toStudentResponse(entity)).thenReturn(response);
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

    /**
     * Asserts a BAD_REQUEST validation failure and confirms zero interaction with either
     * repository, proving the validation short-circuits before any persistence call.
     */
    private void assertValidationFailureWithNoRepositoryInteraction(Executable executable, String expectedMessage) {
        assertApiExceptionThrown(executable, expectedMessage, HttpStatus.BAD_REQUEST);
        verifyNoInteractions(studentRepository);
        verifyNoInteractions(divisionRepository);
    }

    // ==========================================================================================
    // getAllStudents()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllStudents()")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return full student list successfully when students exist")
        void getAllStudents_Success_ReturnsPopulatedStudentList() {
            StudentEntity secondEntity = buildStudentEntity(11, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, divisionEntity);
            StudentResponse secondResponse = buildStudentResponse(11, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            when(studentRepository.findAll()).thenReturn(Arrays.asList(studentEntity, secondEntity));
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(studentResponse);
            when(mapInterface.toStudentResponse(secondEntity)).thenReturn(secondResponse);

            ResponseEntity<Object> result = studentService.getAllStudents();

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size());

            verify(studentRepository, times(1)).findAll();
            verify(mapInterface, times(1)).toStudentResponse(studentEntity);
            verify(mapInterface, times(1)).toStudentResponse(secondEntity);
        }

        @Test
        @DisplayName("Should return an empty list successfully when no students exist")
        void getAllStudents_Success_ReturnsEmptyList_WhenNoneExist() {
            when(studentRepository.findAll()).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = studentService.getAllStudents();

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toStudentResponse(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should propagate exception when repository throws an unexpected error")
        void getAllStudents_Failure_WhenRepositoryThrowsException() {
            when(studentRepository.findAll()).thenThrow(new RuntimeException("Database unavailable"));

            assertThrows(RuntimeException.class, () -> studentService.getAllStudents());

            verify(mapInterface, never()).toStudentResponse(any(StudentEntity.class));
        }
    }

    // ==========================================================================================
    // getAllStudentsOfSchool()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllStudentsOfSchool()")
    class GetAllStudentsOfSchoolTests {

        @Test
        @DisplayName("Should return student list successfully for a valid schoolId")
        void getAllStudentsOfSchool_Success_ReturnsPopulatedList() {
            GetAllStudentRequest request = buildGetAllStudentRequest(VALID_SCHOOL_ID, null, null);

            when(studentRepository.findAllByDivisionEntity_StandardEntity_SchoolEntity_Id(VALID_SCHOOL_ID))
                    .thenReturn(Arrays.asList(studentEntity));
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(studentResponse);

            ResponseEntity<Object> result = studentService.getAllStudentsOfSchool(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(1, data.size());

            verify(studentRepository, times(1)).findAllByDivisionEntity_StandardEntity_SchoolEntity_Id(VALID_SCHOOL_ID);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the school has no students")
        void getAllStudentsOfSchool_Success_ReturnsEmptyList_WhenNoneExist() {
            GetAllStudentRequest request = buildGetAllStudentRequest(VALID_SCHOOL_ID, null, null);

            when(studentRepository.findAllByDivisionEntity_StandardEntity_SchoolEntity_Id(VALID_SCHOOL_ID))
                    .thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = studentService.getAllStudentsOfSchool(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when request is null, without any repository interaction")
        void getAllStudentsOfSchool_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfSchool(null),
                    "Get all student request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when schoolId is negative, without any repository interaction")
        void getAllStudentsOfSchool_Failure_WhenSchoolIdIsNegative() {
            GetAllStudentRequest request = buildGetAllStudentRequest(NEGATIVE_SCHOOL_ID, null, null);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfSchool(request),
                    "School id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // getAllStudentsOfDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllStudentsOfDivision()")
    class GetAllStudentsOfDivisionTests {

        @Test
        @DisplayName("Should return student list successfully for a valid divisionId")
        void getAllStudentsOfDivision_Success_ReturnsPopulatedList() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, VALID_DIVISION_ID, null);

            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID))
                    .thenReturn(Arrays.asList(studentEntity));
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(studentResponse);

            ResponseEntity<Object> result = studentService.getAllStudentsOfDivision(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(1, data.size());

            verify(studentRepository, times(1)).findAllByDivisionEntityId(VALID_DIVISION_ID);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the division has no students")
        void getAllStudentsOfDivision_Success_ReturnsEmptyList_WhenNoneExist() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, VALID_DIVISION_ID, null);

            when(studentRepository.findAllByDivisionEntityId(VALID_DIVISION_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = studentService.getAllStudentsOfDivision(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when request is null, without any repository interaction")
        void getAllStudentsOfDivision_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfDivision(null),
                    "Get all student request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when divisionId is negative, without any repository interaction")
        void getAllStudentsOfDivision_Failure_WhenDivisionIdIsNegative() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, NEGATIVE_DIVISION_ID, null);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfDivision(request),
                    "Division id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // getAllStudentsOfStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllStudentsOfStandard()")
    class GetAllStudentsOfStandardTests {

        @Test
        @DisplayName("Should return student list successfully for a valid standardId")
        void getAllStudentsOfStandard_Success_ReturnsPopulatedList() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, null, VALID_STANDARD_ID);

            when(studentRepository.findAllByDivisionEntity_StandardEntity_Id(VALID_STANDARD_ID))
                    .thenReturn(Arrays.asList(studentEntity));
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(studentResponse);

            ResponseEntity<Object> result = studentService.getAllStudentsOfStandard(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(1, data.size());

            verify(studentRepository, times(1)).findAllByDivisionEntity_StandardEntity_Id(VALID_STANDARD_ID);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the standard has no students")
        void getAllStudentsOfStandard_Success_ReturnsEmptyList_WhenNoneExist() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, null, VALID_STANDARD_ID);

            when(studentRepository.findAllByDivisionEntity_StandardEntity_Id(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = studentService.getAllStudentsOfStandard(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when request is null, without any repository interaction")
        void getAllStudentsOfStandard_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfStandard(null),
                    "Get all student request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standardId is negative, without any repository interaction")
        void getAllStudentsOfStandard_Failure_WhenStandardIdIsNegative() {
            GetAllStudentRequest request = buildGetAllStudentRequest(null, null, NEGATIVE_STANDARD_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getAllStudentsOfStandard(request),
                    "Standard id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // addStudent()
    // ==========================================================================================

    @Nested
    @DisplayName("addStudent()")
    class AddStudentTests {

        @Test
        @DisplayName("Should add a new student successfully when the name is unique for the division")
        void addStudent_Success_WhenStudentIsUnique() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            when(studentRepository.findByStudentNameAndDivisionEntity_Id(VALID_STUDENT_NAME, VALID_DIVISION_ID))
                    .thenReturn(null);
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(mapInterface.toStudentEntity(request)).thenReturn(studentEntity);
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(studentResponse);

            ResponseEntity<Object> result = studentService.addStudent(request, VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("new student added", ((ApiResponse) result.getBody()).getMessage());
            assertEquals(divisionEntity, studentEntity.getDivisionEntity());

            verify(studentRepository, times(1)).findByStudentNameAndDivisionEntity_Id(VALID_STUDENT_NAME, VALID_DIVISION_ID);
            verify(divisionRepository, times(1)).findById(VALID_DIVISION_ID);
            verify(studentRepository, times(1)).save(studentEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the parent division does not exist")
        void addStudent_Failure_WhenDivisionNotFound() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            when(studentRepository.findByStudentNameAndDivisionEntity_Id(VALID_STUDENT_NAME, NON_EXISTENT_DIVISION_ID))
                    .thenReturn(null);
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> studentService.addStudent(request, NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(studentRepository, never()).save(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should throw CONFLICT ApiException when the student name already exists for the division")
        void addStudent_Failure_WhenStudentAlreadyExists() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            when(studentRepository.findByStudentNameAndDivisionEntity_Id(VALID_STUDENT_NAME, VALID_DIVISION_ID))
                    .thenReturn(studentEntity);

            assertApiExceptionThrown(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Student already exist",
                    HttpStatus.CONFLICT
            );

            verify(divisionRepository, never()).findById(anyInt());
            verify(studentRepository, never()).save(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when divisionId is negative, without any repository interaction")
        void addStudent_Failure_WhenDivisionIdIsNegative() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, NEGATIVE_DIVISION_ID),
                    "Division id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when StudentRequest is null")
        void addStudent_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(null, VALID_DIVISION_ID),
                    "Student request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when student name is blank")
        void addStudent_Failure_WhenStudentNameIsBlank() {
            StudentRequest request = buildStudentRequest(BLANK_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Student name must not be blank"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when obtainMarks is negative")
        void addStudent_Failure_WhenObtainMarksIsNegative() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, NEGATIVE_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Obtained marks must not be negative"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when percentage is negative")
        void addStudent_Failure_WhenPercentageIsNegative() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, NEGATIVE_PERCENTAGE, VALID_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Percentage must be between 0 and 100"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when percentage exceeds 100")
        void addStudent_Failure_WhenPercentageExceeds100() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, OVER_100_PERCENTAGE, VALID_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Percentage must be between 0 and 100"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when result is blank")
        void addStudent_Failure_WhenResultIsBlank() {
            StudentRequest request = buildStudentRequest(VALID_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, BLANK_RESULT);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.addStudent(request, VALID_DIVISION_ID),
                    "Result must not be blank"
            );
        }
    }

    // ==========================================================================================
    // getStudentById()
    // ==========================================================================================

    @Nested
    @DisplayName("getStudentById()")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return student successfully when a valid student id is provided")
        void getStudentById_Success_WhenStudentExists() {
            when(studentRepository.findById(VALID_STUDENT_ID)).thenReturn(Optional.of(studentEntity));
            mockSuccessfulEntityToResponseMapping(studentEntity, studentResponse);

            ResponseEntity<Object> result = studentService.getStudentById(VALID_STUDENT_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals(studentResponse, apiResponse.getData());
            verify(studentRepository, times(1)).findById(VALID_STUDENT_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when student id does not exist")
        void getStudentById_Failure_WhenStudentNotFound() {
            when(studentRepository.findById(NON_EXISTENT_STUDENT_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> studentService.getStudentById(NON_EXISTENT_STUDENT_ID),
                    "Student not found with id=" + NON_EXISTENT_STUDENT_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(mapInterface, never()).toStudentResponse(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when student id is negative, without querying the repository")
        void getStudentById_Failure_WhenStudentIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.getStudentById(NEGATIVE_STUDENT_ID),
                    "Student id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // deleteStudent()
    // ==========================================================================================

    @Nested
    @DisplayName("deleteStudent()")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should delete student successfully when a valid student id is provided")
        void deleteStudent_Success_WhenStudentExists() {
            when(studentRepository.findById(VALID_STUDENT_ID)).thenReturn(Optional.of(studentEntity));
            doNothing().when(studentRepository).deleteById(VALID_STUDENT_ID);

            ResponseEntity<Object> result = studentService.deleteStudent(VALID_STUDENT_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals("Student Deleted", apiResponse.getData());
            assertEquals("successful", apiResponse.getMessage());

            verify(studentRepository, times(1)).findById(VALID_STUDENT_ID);
            verify(studentRepository, times(1)).deleteById(VALID_STUDENT_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when attempting to delete a non-existent student")
        void deleteStudent_Failure_WhenStudentNotFound() {
            when(studentRepository.findById(NON_EXISTENT_STUDENT_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> studentService.deleteStudent(NON_EXISTENT_STUDENT_ID),
                    "Student not found with id=" + NON_EXISTENT_STUDENT_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(studentRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when student id is negative, without any repository interaction")
        void deleteStudent_Failure_WhenStudentIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.deleteStudent(NEGATIVE_STUDENT_ID),
                    "Student id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // modifyStudent()
    // ==========================================================================================

    @Nested
    @DisplayName("modifyStudent()")
    class ModifyStudentTests {

        @Test
        @DisplayName("Should modify student successfully when a valid id and dto are provided")
        void modifyStudent_Success_WhenStudentExists() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);
            StudentResponse updatedResponse = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            when(studentRepository.findById(VALID_STUDENT_ID)).thenReturn(Optional.of(studentEntity));
            when(mapInterface.toStudentResponse(studentEntity)).thenReturn(updatedResponse);

            ResponseEntity<Object> result = studentService.modifyStudent(dto, VALID_STUDENT_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_STUDENT_NAME, studentEntity.getStudentName());

            verify(studentRepository, times(1)).findById(VALID_STUDENT_ID);
            verify(studentRepository, times(1)).save(studentEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when modifying a non-existent student")
        void modifyStudent_Failure_WhenStudentNotFound() {
            StudentResponse dto = buildStudentResponse(NON_EXISTENT_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);
            when(studentRepository.findById(NON_EXISTENT_STUDENT_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> studentService.modifyStudent(dto, NON_EXISTENT_STUDENT_ID),
                    "Student not found with id=" + NON_EXISTENT_STUDENT_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(studentRepository, never()).save(any(StudentEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when student id is negative, without any repository interaction")
        void modifyStudent_Failure_WhenStudentIdIsNegative() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, NEGATIVE_STUDENT_ID),
                    "Student id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when StudentResponse dto is null")
        void modifyStudent_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(null, VALID_STUDENT_ID),
                    "Student request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated student name is blank")
        void modifyStudent_Failure_WhenStudentNameIsBlank() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, BLANK_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, VALID_STUDENT_ID),
                    "Student name must not be blank"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated obtainMarks is negative")
        void modifyStudent_Failure_WhenObtainMarksIsNegative() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, NEGATIVE_OBTAIN_MARKS, VALID_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, VALID_STUDENT_ID),
                    "Obtained marks must not be negative"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated percentage is negative")
        void modifyStudent_Failure_WhenPercentageIsNegative() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, NEGATIVE_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, VALID_STUDENT_ID),
                    "Percentage must be between 0 and 100"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated percentage exceeds 100")
        void modifyStudent_Failure_WhenPercentageExceeds100() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, OVER_100_PERCENTAGE, VALID_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, VALID_STUDENT_ID),
                    "Percentage must be between 0 and 100"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated result is blank")
        void modifyStudent_Failure_WhenResultIsBlank() {
            StudentResponse dto = buildStudentResponse(VALID_STUDENT_ID, UPDATED_STUDENT_NAME, VALID_OBTAIN_MARKS, VALID_PERCENTAGE, BLANK_RESULT, VALID_DIVISION_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> studentService.modifyStudent(dto, VALID_STUDENT_ID),
                    "Result must not be blank"
            );
        }
    }
}
