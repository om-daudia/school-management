package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SubjectMarkRequest;
import com.odschool.dtos.SubjectMarkResponse;
import com.odschool.entity.StudentEntity;
import com.odschool.entity.SubjectMarkEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.StudentRepository;
import com.odschool.repository.SubjectMarkRepository;
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
@DisplayName("SubjectMarkService Unit Tests")
class SubjectMarkServiceTest {

    private static final int VALID_STUDENT_ID = 1;
    private static final int NON_EXISTENT_STUDENT_ID = 888;
    private static final int NEGATIVE_STUDENT_ID = -1;

    private static final int VALID_SUBJECT_MARK_ID = 10;
    private static final int NON_EXISTENT_SUBJECT_MARK_ID = 999;
    private static final int NEGATIVE_SUBJECT_MARK_ID = -1;

    private static final String VALID_SUBJECT_NAME = "Mathematics";
    private static final String UPDATED_SUBJECT_NAME = "Science";
    private static final String BLANK_SUBJECT_NAME = "   ";

    private static final double VALID_MARKS = 85;
    private static final double UPDATED_MARKS = 90;
    private static final double NEGATIVE_MARKS = -5;

    @Mock
    private SubjectMarkRepository subjectMarkRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private SubjectMarkService subjectMarkService;

    private StudentEntity studentEntity;
    private SubjectMarkEntity subjectMarkEntity;
    private SubjectMarkResponse subjectMarkResponse;

    @BeforeEach
    void setUp() {
        studentEntity = buildStudentEntity(VALID_STUDENT_ID);
        subjectMarkEntity = buildSubjectMarkEntity(VALID_SUBJECT_MARK_ID, VALID_SUBJECT_NAME, VALID_MARKS, studentEntity);
        subjectMarkResponse = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, VALID_SUBJECT_NAME, VALID_MARKS, VALID_STUDENT_ID);
    }

    // ==========================================================================================
    // Common / reusable helper methods
    // ==========================================================================================

    private StudentEntity buildStudentEntity(int id) {
        StudentEntity entity = new StudentEntity();
        entity.setId(id);
        return entity;
    }

    private SubjectMarkEntity buildSubjectMarkEntity(int id, String subjectName, double marks, StudentEntity studentEntity) {
        SubjectMarkEntity entity = new SubjectMarkEntity();
        entity.setId(id);
        entity.setSubjectName(subjectName);
        entity.setMarks(marks);
        entity.setStudentEntity(studentEntity);
        return entity;
    }

    private SubjectMarkResponse buildSubjectMarkResponse(int id, String subjectName, double marks, int studentId) {
        SubjectMarkResponse response = new SubjectMarkResponse();
        response.setId(id);
        response.setSubjectName(subjectName);
        response.setMarks(marks);
        response.setStudentId(studentId);
        return response;
    }

    private SubjectMarkRequest buildSubjectMarkRequest(String subjectName, double marks, int studentId) {
        SubjectMarkRequest request = new SubjectMarkRequest();
        request.setSubjectName(subjectName);
        request.setMarks(marks);
        request.setStudentId(studentId);
        return request;
    }

    private void mockSuccessfulEntityToResponseMapping(SubjectMarkEntity entity, SubjectMarkResponse response) {
        when(mapInterface.toSubjectMarkResponse(entity)).thenReturn(response);
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
        verifyNoInteractions(subjectMarkRepository);
        verifyNoInteractions(studentRepository);
    }

    // ==========================================================================================
    // getAllSubjectMarks()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllSubjectMarks()")
    class GetAllSubjectMarksTests {

        @Test
        @DisplayName("Should return full subject mark list successfully when marks exist for the student")
        void getAllSubjectMarks_Success_ReturnsPopulatedSubjectMarkList() {
            SubjectMarkEntity secondEntity = buildSubjectMarkEntity(11, UPDATED_SUBJECT_NAME, UPDATED_MARKS, studentEntity);
            SubjectMarkResponse secondResponse = buildSubjectMarkResponse(11, UPDATED_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);

            when(subjectMarkRepository.findAllByStudentEntity_Id(VALID_STUDENT_ID))
                    .thenReturn(Arrays.asList(subjectMarkEntity, secondEntity));
            when(mapInterface.toSubjectMarkResponse(subjectMarkEntity)).thenReturn(subjectMarkResponse);
            when(mapInterface.toSubjectMarkResponse(secondEntity)).thenReturn(secondResponse);

            ResponseEntity<Object> result = subjectMarkService.getAllSubjectMarks(VALID_STUDENT_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size());

            verify(subjectMarkRepository, times(1)).findAllByStudentEntity_Id(VALID_STUDENT_ID);
            verify(mapInterface, times(1)).toSubjectMarkResponse(subjectMarkEntity);
            verify(mapInterface, times(1)).toSubjectMarkResponse(secondEntity);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the student has no subject marks")
        void getAllSubjectMarks_Success_ReturnsEmptyList_WhenNoneExist() {
            when(subjectMarkRepository.findAllByStudentEntity_Id(VALID_STUDENT_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = subjectMarkService.getAllSubjectMarks(VALID_STUDENT_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toSubjectMarkResponse(any(SubjectMarkEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when studentId is negative, without querying either repository")
        void getAllSubjectMarks_Failure_WhenStudentIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.getAllSubjectMarks(NEGATIVE_STUDENT_ID),
                    "Student id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should propagate exception when repository throws an unexpected error")
        void getAllSubjectMarks_Failure_WhenRepositoryThrowsException() {
            when(subjectMarkRepository.findAllByStudentEntity_Id(VALID_STUDENT_ID))
                    .thenThrow(new RuntimeException("Database unavailable"));

            assertThrows(RuntimeException.class, () -> subjectMarkService.getAllSubjectMarks(VALID_STUDENT_ID));

            verify(mapInterface, never()).toSubjectMarkResponse(any(SubjectMarkEntity.class));
        }
    }

    // ==========================================================================================
    // addSubjectMark()
    // ==========================================================================================

    @Nested
    @DisplayName("addSubjectMark()")
    class AddSubjectMarkTests {

        @Test
        @DisplayName("Should add a new subject mark successfully when it does not already exist for the student")
        void addSubjectMark_Success_WhenSubjectMarkIsUnique() {
            SubjectMarkRequest request = buildSubjectMarkRequest(VALID_SUBJECT_NAME, VALID_MARKS, VALID_STUDENT_ID);

            when(subjectMarkRepository.findBySubjectNameAndStudentEntity_Id(VALID_SUBJECT_NAME, VALID_STUDENT_ID))
                    .thenReturn(null);
            when(studentRepository.findById(VALID_STUDENT_ID)).thenReturn(Optional.of(studentEntity));
            when(mapInterface.toSubjectMarkEntity(request)).thenReturn(subjectMarkEntity);
            when(mapInterface.toSubjectMarkResponse(subjectMarkEntity)).thenReturn(subjectMarkResponse);

            ResponseEntity<Object> result = subjectMarkService.addSubjectMark(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("new subject mark added", ((ApiResponse) result.getBody()).getMessage());
            assertEquals(studentEntity, subjectMarkEntity.getStudentEntity());

            verify(subjectMarkRepository, times(1)).findBySubjectNameAndStudentEntity_Id(VALID_SUBJECT_NAME, VALID_STUDENT_ID);
            verify(studentRepository, times(1)).findById(VALID_STUDENT_ID);
            verify(subjectMarkRepository, times(1)).save(subjectMarkEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the parent student does not exist")
        void addSubjectMark_Failure_WhenStudentNotFound() {
            SubjectMarkRequest request = buildSubjectMarkRequest(VALID_SUBJECT_NAME, VALID_MARKS, NON_EXISTENT_STUDENT_ID);

            when(subjectMarkRepository.findBySubjectNameAndStudentEntity_Id(VALID_SUBJECT_NAME, NON_EXISTENT_STUDENT_ID))
                    .thenReturn(null);
            when(studentRepository.findById(NON_EXISTENT_STUDENT_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> subjectMarkService.addSubjectMark(request),
                    "Student not found with id=" + NON_EXISTENT_STUDENT_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(subjectMarkRepository, never()).save(any(SubjectMarkEntity.class));
        }

        @Test
        @DisplayName("Should throw CONFLICT ApiException when the subject mark already exists for the student")
        void addSubjectMark_Failure_WhenSubjectMarkAlreadyExists() {
            SubjectMarkRequest request = buildSubjectMarkRequest(VALID_SUBJECT_NAME, VALID_MARKS, VALID_STUDENT_ID);

            when(subjectMarkRepository.findBySubjectNameAndStudentEntity_Id(VALID_SUBJECT_NAME, VALID_STUDENT_ID))
                    .thenReturn(subjectMarkEntity);

            assertApiExceptionThrown(
                    () -> subjectMarkService.addSubjectMark(request),
                    "Subject mark already exist",
                    HttpStatus.CONFLICT
            );

            verify(studentRepository, never()).findById(anyInt());
            verify(subjectMarkRepository, never()).save(any(SubjectMarkEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when SubjectMarkRequest is null")
        void addSubjectMark_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.addSubjectMark(null),
                    "Subject mark request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when studentId in the request is negative")
        void addSubjectMark_Failure_WhenStudentIdIsNegative() {
            SubjectMarkRequest request = buildSubjectMarkRequest(VALID_SUBJECT_NAME, VALID_MARKS, NEGATIVE_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.addSubjectMark(request),
                    "Student id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when subject name is blank")
        void addSubjectMark_Failure_WhenSubjectNameIsBlank() {
            SubjectMarkRequest request = buildSubjectMarkRequest(BLANK_SUBJECT_NAME, VALID_MARKS, VALID_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.addSubjectMark(request),
                    "Subject name must not be blank"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when marks is negative")
        void addSubjectMark_Failure_WhenMarksIsNegative() {
            SubjectMarkRequest request = buildSubjectMarkRequest(VALID_SUBJECT_NAME, NEGATIVE_MARKS, VALID_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.addSubjectMark(request),
                    "Marks must not be negative"
            );
        }
    }

    // ==========================================================================================
    // getSubjectMarkById()
    // ==========================================================================================

    @Nested
    @DisplayName("getSubjectMarkById()")
    class GetSubjectMarkByIdTests {

        @Test
        @DisplayName("Should return subject mark successfully when a valid subject mark id is provided")
        void getSubjectMarkById_Success_WhenSubjectMarkExists() {
            when(subjectMarkRepository.findById(VALID_SUBJECT_MARK_ID)).thenReturn(Optional.of(subjectMarkEntity));
            mockSuccessfulEntityToResponseMapping(subjectMarkEntity, subjectMarkResponse);

            ResponseEntity<Object> result = subjectMarkService.getSubjectMarkById(VALID_SUBJECT_MARK_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals(subjectMarkResponse, apiResponse.getData());
            verify(subjectMarkRepository, times(1)).findById(VALID_SUBJECT_MARK_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when subject mark id does not exist")
        void getSubjectMarkById_Failure_WhenSubjectMarkNotFound() {
            when(subjectMarkRepository.findById(NON_EXISTENT_SUBJECT_MARK_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> subjectMarkService.getSubjectMarkById(NON_EXISTENT_SUBJECT_MARK_ID),
                    "Subject Mark not found with id=" + NON_EXISTENT_SUBJECT_MARK_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(mapInterface, never()).toSubjectMarkResponse(any(SubjectMarkEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when subject mark id is negative, without querying the repository")
        void getSubjectMarkById_Failure_WhenSubjectMarkIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.getSubjectMarkById(NEGATIVE_SUBJECT_MARK_ID),
                    "Subject mark id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // deleteSubjectMark()
    // ==========================================================================================

    @Nested
    @DisplayName("deleteSubjectMark()")
    class DeleteSubjectMarkTests {

        @Test
        @DisplayName("Should delete subject mark successfully when a valid subject mark id is provided")
        void deleteSubjectMark_Success_WhenSubjectMarkExists() {
            when(subjectMarkRepository.findById(VALID_SUBJECT_MARK_ID)).thenReturn(Optional.of(subjectMarkEntity));
            doNothing().when(subjectMarkRepository).deleteById(VALID_SUBJECT_MARK_ID);

            ResponseEntity<Object> result = subjectMarkService.deleteSubjectMark(VALID_SUBJECT_MARK_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals("Subject Mark Deleted", apiResponse.getData());
            assertEquals("successful", apiResponse.getMessage());

            verify(subjectMarkRepository, times(1)).findById(VALID_SUBJECT_MARK_ID);
            verify(subjectMarkRepository, times(1)).deleteById(VALID_SUBJECT_MARK_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when attempting to delete a non-existent subject mark")
        void deleteSubjectMark_Failure_WhenSubjectMarkNotFound() {
            when(subjectMarkRepository.findById(NON_EXISTENT_SUBJECT_MARK_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> subjectMarkService.deleteSubjectMark(NON_EXISTENT_SUBJECT_MARK_ID),
                    "Subject Mark not found with id=" + NON_EXISTENT_SUBJECT_MARK_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(subjectMarkRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when subject mark id is negative, without any repository interaction")
        void deleteSubjectMark_Failure_WhenSubjectMarkIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.deleteSubjectMark(NEGATIVE_SUBJECT_MARK_ID),
                    "Subject mark id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // modifySubjectMark()
    // ==========================================================================================

    @Nested
    @DisplayName("modifySubjectMark()")
    class ModifySubjectMarkTests {

        @Test
        @DisplayName("Should modify subject mark successfully when a valid id and dto are provided")
        void modifySubjectMark_Success_WhenSubjectMarkExists() {
            SubjectMarkResponse dto = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, UPDATED_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);
            SubjectMarkResponse updatedResponse = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, UPDATED_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);

            when(subjectMarkRepository.findById(VALID_SUBJECT_MARK_ID)).thenReturn(Optional.of(subjectMarkEntity));
            when(mapInterface.toSubjectMarkResponse(subjectMarkEntity)).thenReturn(updatedResponse);

            ResponseEntity<Object> result = subjectMarkService.modifySubjectMark(dto, VALID_SUBJECT_MARK_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_SUBJECT_NAME, subjectMarkEntity.getSubjectName());
            assertEquals(UPDATED_MARKS, subjectMarkEntity.getMarks());

            verify(subjectMarkRepository, times(1)).findById(VALID_SUBJECT_MARK_ID);
            verify(subjectMarkRepository, times(1)).save(subjectMarkEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when modifying a non-existent subject mark")
        void modifySubjectMark_Failure_WhenSubjectMarkNotFound() {
            SubjectMarkResponse dto = buildSubjectMarkResponse(NON_EXISTENT_SUBJECT_MARK_ID, UPDATED_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);
            when(subjectMarkRepository.findById(NON_EXISTENT_SUBJECT_MARK_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> subjectMarkService.modifySubjectMark(dto, NON_EXISTENT_SUBJECT_MARK_ID),
                    "Subject Mark not found with id=" + NON_EXISTENT_SUBJECT_MARK_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(subjectMarkRepository, never()).save(any(SubjectMarkEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when subject mark id is negative, without any repository interaction")
        void modifySubjectMark_Failure_WhenSubjectMarkIdIsNegative() {
            SubjectMarkResponse dto = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, UPDATED_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.modifySubjectMark(dto, NEGATIVE_SUBJECT_MARK_ID),
                    "Subject mark id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when SubjectMarkResponse dto is null")
        void modifySubjectMark_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.modifySubjectMark(null, VALID_SUBJECT_MARK_ID),
                    "Subject mark request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated subject name is blank")
        void modifySubjectMark_Failure_WhenSubjectNameIsBlank() {
            SubjectMarkResponse dto = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, BLANK_SUBJECT_NAME, UPDATED_MARKS, VALID_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.modifySubjectMark(dto, VALID_SUBJECT_MARK_ID),
                    "Subject name must not be blank"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated marks is negative")
        void modifySubjectMark_Failure_WhenMarksIsNegative() {
            SubjectMarkResponse dto = buildSubjectMarkResponse(VALID_SUBJECT_MARK_ID, UPDATED_SUBJECT_NAME, NEGATIVE_MARKS, VALID_STUDENT_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> subjectMarkService.modifySubjectMark(dto, VALID_SUBJECT_MARK_ID),
                    "Marks must not be negative"
            );
        }
    }
}
