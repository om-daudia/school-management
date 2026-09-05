package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SchoolRequest;
import com.odschool.dtos.SchoolResponse;
import com.odschool.entity.SchoolEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.SchoolRepository;
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
@DisplayName("SchoolService Unit Tests")
class SchoolServiceTest {

    private static final int VALID_SCHOOL_ID = 1;
    private static final int NON_EXISTENT_SCHOOL_ID = 999;
    private static final int ZERO_SCHOOL_ID = 0;
    private static final int NEGATIVE_SCHOOL_ID = -1;
    private static final String VALID_SCHOOL_NAME = "Springfield Elementary";
    private static final String UPDATED_SCHOOL_NAME = "Springfield High";
    private static final String EMPTY_SCHOOL_NAME = "";
    private static final String BLANK_SCHOOL_NAME = "   ";

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private SchoolService schoolService;

    private SchoolEntity schoolEntity;
    private SchoolResponse schoolResponse;

    @BeforeEach
    void setUp() {
        schoolEntity = buildSchoolEntity(VALID_SCHOOL_ID, VALID_SCHOOL_NAME);
        schoolResponse = buildSchoolResponse(VALID_SCHOOL_ID, VALID_SCHOOL_NAME);
    }

    // ==========================================================================================
    // Common / reusable helper methods
    // ==========================================================================================

    private SchoolEntity buildSchoolEntity(int id, String schoolName) {
        SchoolEntity entity = new SchoolEntity(schoolName);
        entity.setId(id);
        return entity;
    }

    private SchoolResponse buildSchoolResponse(int id, String schoolName) {
        SchoolResponse response = new SchoolResponse();
        response.setId(id);
        response.setSchoolName(schoolName);
        return response;
    }

    private SchoolRequest buildSchoolRequest(String schoolName) {
        SchoolRequest request = new SchoolRequest();
        request.setSchoolName(schoolName);
        return request;
    }

    private void mockSuccessfulEntityToResponseMapping(SchoolEntity entity, SchoolResponse response) {
        when(mapInterface.toSchoolResponse(entity)).thenReturn(response);
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

    private void assertValidationFailureWithNoRepositoryInteraction(Executable executable, String expectedMessage) {
        assertApiExceptionThrown(executable, expectedMessage, HttpStatus.BAD_REQUEST);
        verifyNoInteractions(schoolRepository);
    }

    // ==========================================================================================
    // getAllSchools()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllSchools()")
    class GetAllSchoolsTests {

        @Test
        @DisplayName("Should return full school list successfully when schools exist")
        void getAllSchools_Success_ReturnsPopulatedSchoolList() {
            SchoolEntity secondEntity = buildSchoolEntity(2, "Shelbyville High");
            SchoolResponse secondResponse = buildSchoolResponse(2, "Shelbyville High");

            when(schoolRepository.findAll()).thenReturn(Arrays.asList(schoolEntity, secondEntity));
            when(mapInterface.toSchoolResponse(schoolEntity)).thenReturn(schoolResponse);
            when(mapInterface.toSchoolResponse(secondEntity)).thenReturn(secondResponse);

            ResponseEntity<Object> result = schoolService.getAllSchools();

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size());

            verify(schoolRepository, times(1)).findAll();
            verify(mapInterface, times(1)).toSchoolResponse(schoolEntity);
            verify(mapInterface, times(1)).toSchoolResponse(secondEntity);
        }

        @Test
        @DisplayName("Should return an empty list successfully when no schools exist")
        void getAllSchools_Success_ReturnsEmptyList_WhenRepositoryIsEmpty() {
            when(schoolRepository.findAll()).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = schoolService.getAllSchools();

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toSchoolResponse(any(SchoolEntity.class));
        }

        @Test
        @DisplayName("Should propagate exception when repository throws an unexpected error")
        void getAllSchools_Failure_WhenRepositoryThrowsException() {
            when(schoolRepository.findAll()).thenThrow(new RuntimeException("Database unavailable"));

            assertThrows(RuntimeException.class, () -> schoolService.getAllSchools());

            verify(mapInterface, never()).toSchoolResponse(any(SchoolEntity.class));
        }
    }

    // ==========================================================================================
    // addSchool()
    // ==========================================================================================

    @Nested
    @DisplayName("addSchool()")
    class AddSchoolTests {

        @Test
        @DisplayName("Should return response containing the correct success message")
        void addSchool_Success_WhenSchoolNameIsUnique() {
            SchoolRequest request = buildSchoolRequest(VALID_SCHOOL_NAME);
            when(schoolRepository.findBySchoolName(VALID_SCHOOL_NAME)).thenReturn(null);
            when(mapInterface.toSchoolResponse(any(SchoolEntity.class))).thenReturn(schoolResponse);

            ResponseEntity<Object> result = schoolService.addSchool(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("new school added", ((ApiResponse) result.getBody()).getMessage());
            verify(schoolRepository, times(1)).findBySchoolName(VALID_SCHOOL_NAME);
            verify(schoolRepository, times(1)).save(any(SchoolEntity.class));
        }

        @Test
        @DisplayName("Should trim surrounding whitespace from the school name before saving")
        void addSchool_Success_TrimsWhitespaceFromSchoolName() {
            SchoolRequest request = buildSchoolRequest("  " + VALID_SCHOOL_NAME + "  ");
            when(schoolRepository.findBySchoolName(VALID_SCHOOL_NAME)).thenReturn(null);
            when(mapInterface.toSchoolResponse(any(SchoolEntity.class))).thenReturn(schoolResponse);

            ResponseEntity<Object> result = schoolService.addSchool(request);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            verify(schoolRepository, times(1)).findBySchoolName(VALID_SCHOOL_NAME);
            verify(schoolRepository, times(1)).save(argThat(
                    entity -> VALID_SCHOOL_NAME.equals(entity.getSchoolName())));
        }

        @Test
        @DisplayName("Should throw CONFLICT ApiException when school name already exists")
        void addSchool_Failure_WhenSchoolAlreadyExists() {
            SchoolRequest request = buildSchoolRequest(VALID_SCHOOL_NAME);
            when(schoolRepository.findBySchoolName(VALID_SCHOOL_NAME)).thenReturn(schoolEntity);

            assertApiExceptionThrown(
                    () -> schoolService.addSchool(request),
                    "School already exist",
                    HttpStatus.CONFLICT
            );

            verify(schoolRepository, never()).save(any(SchoolEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when SchoolRequest is null")
        void addSchool_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.addSchool(null),
                    "School request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school name field is null")
        void addSchool_Failure_WhenSchoolNameIsNull() {
            SchoolRequest request = buildSchoolRequest(null);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.addSchool(request),
                    "School name must not be null or empty"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school name field is empty")
        void addSchool_Failure_WhenSchoolNameIsEmpty() {
            SchoolRequest request = buildSchoolRequest(EMPTY_SCHOOL_NAME);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.addSchool(request),
                    "School name must not be null or empty"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school name field is blank/whitespace-only")
        void addSchool_Failure_WhenSchoolNameIsBlank() {
            SchoolRequest request = buildSchoolRequest(BLANK_SCHOOL_NAME);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.addSchool(request),
                    "School name must not be null or empty"
            );
        }
    }

    // ==========================================================================================
    // getSchoolById()
    // ==========================================================================================

    @Nested
    @DisplayName("getSchoolById()")
    class GetSchoolByIdTests {

        @Test
        @DisplayName("Should return school successfully when a valid school id is provided")
        void getSchoolById_Success_WhenSchoolExists() {
            when(schoolRepository.findById(VALID_SCHOOL_ID)).thenReturn(Optional.of(schoolEntity));
            mockSuccessfulEntityToResponseMapping(schoolEntity, schoolResponse);

            ResponseEntity<Object> result = schoolService.getSchoolById(VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals(schoolResponse, apiResponse.getData());
            verify(schoolRepository, times(1)).findById(VALID_SCHOOL_ID);
        }

        @Test
        @DisplayName("Should throw ApiException when school id does not exist")
        void getSchoolById_Failure_WhenSchoolNotFound() {
            when(schoolRepository.findById(NON_EXISTENT_SCHOOL_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> schoolService.getSchoolById(NON_EXISTENT_SCHOOL_ID),
                    "School not found with id=" + NON_EXISTENT_SCHOOL_ID,
                    HttpStatus.OK
            );

            verify(mapInterface, never()).toSchoolResponse(any(SchoolEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school id is invalid, without querying the repository")
        void getSchoolById_Failure_WhenSchoolIdIsInvalid() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.getSchoolById(NEGATIVE_SCHOOL_ID),
                    "School id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // deleteSchool()
    // ==========================================================================================

    @Nested
    @DisplayName("deleteSchool()")
    class DeleteSchoolTests {

        @Test
        @DisplayName("Should delete school successfully when a valid school id is provided")
        void deleteSchool_Success_WhenSchoolExists() {
            when(schoolRepository.findById(VALID_SCHOOL_ID)).thenReturn(Optional.of(schoolEntity));
            doNothing().when(schoolRepository).deleteById(VALID_SCHOOL_ID);

            ResponseEntity<Object> result = schoolService.deleteSchool(VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals("School Deleted", apiResponse.getData());
            assertEquals("successful", apiResponse.getMessage());

            verify(schoolRepository, times(1)).findById(VALID_SCHOOL_ID);
            verify(schoolRepository, times(1)).deleteById(VALID_SCHOOL_ID);
        }

        @Test
        @DisplayName("Should throw ApiException when attempting to delete a non-existent school")
        void deleteSchool_Failure_WhenSchoolNotFound() {
            when(schoolRepository.findById(NON_EXISTENT_SCHOOL_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> schoolService.deleteSchool(NON_EXISTENT_SCHOOL_ID),
                    "School not found with id=" + NON_EXISTENT_SCHOOL_ID,
                    HttpStatus.OK
            );

            verify(schoolRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school id is invalid, without any repository interaction")
        void deleteSchool_Failure_WhenSchoolIdIsInvalid() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.deleteSchool(NEGATIVE_SCHOOL_ID),
                    "School id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // modifySchool()
    // ==========================================================================================

    @Nested
    @DisplayName("modifySchool()")
    class ModifySchoolTests {

        @Test
        @DisplayName("Should modify school successfully when a valid id and dto are provided")
        void modifySchool_Success_WhenSchoolExists() {
            SchoolResponse updateDto = buildSchoolResponse(VALID_SCHOOL_ID, UPDATED_SCHOOL_NAME);
            SchoolResponse updatedResponse = buildSchoolResponse(VALID_SCHOOL_ID, UPDATED_SCHOOL_NAME);

            when(schoolRepository.findById(VALID_SCHOOL_ID)).thenReturn(Optional.of(schoolEntity));
            when(mapInterface.toSchoolResponse(schoolEntity)).thenReturn(updatedResponse);

            ResponseEntity<Object> result = schoolService.modifySchool(updateDto, VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_SCHOOL_NAME, schoolEntity.getSchoolName());

            verify(schoolRepository, times(1)).findById(VALID_SCHOOL_ID);
            verify(schoolRepository, times(1)).save(schoolEntity);
        }

        @Test
        @DisplayName("Should trim surrounding whitespace from the updated school name before saving")
        void modifySchool_Success_TrimsWhitespaceFromSchoolName() {
            SchoolResponse updateDto = buildSchoolResponse(VALID_SCHOOL_ID, "  " + UPDATED_SCHOOL_NAME + "  ");

            when(schoolRepository.findById(VALID_SCHOOL_ID)).thenReturn(Optional.of(schoolEntity));
            when(mapInterface.toSchoolResponse(schoolEntity)).thenReturn(
                    buildSchoolResponse(VALID_SCHOOL_ID, UPDATED_SCHOOL_NAME));

            ResponseEntity<Object> result = schoolService.modifySchool(updateDto, VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_SCHOOL_NAME, schoolEntity.getSchoolName());
        }

        @Test
        @DisplayName("Should throw ApiException when modifying a non-existent school")
        void modifySchool_Failure_WhenSchoolNotFound() {
            SchoolResponse updateDto = buildSchoolResponse(NON_EXISTENT_SCHOOL_ID, UPDATED_SCHOOL_NAME);
            when(schoolRepository.findById(NON_EXISTENT_SCHOOL_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> schoolService.modifySchool(updateDto, NON_EXISTENT_SCHOOL_ID),
                    "School not found with id=" + NON_EXISTENT_SCHOOL_ID,
                    HttpStatus.OK
            );

            verify(schoolRepository, never()).save(any(SchoolEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when school id is negative, without any repository interaction")
        void modifySchool_Failure_WhenSchoolIdIsNegative() {
            SchoolResponse updateDto = buildSchoolResponse(NEGATIVE_SCHOOL_ID, UPDATED_SCHOOL_NAME);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.modifySchool(updateDto, NEGATIVE_SCHOOL_ID),
                    "School id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when SchoolResponse dto is null")
        void modifySchool_Failure_WhenDtoIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.modifySchool(null, VALID_SCHOOL_ID),
                    "School data must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated school name is null")
        void modifySchool_Failure_WhenSchoolNameIsNull() {
            SchoolResponse updateDto = buildSchoolResponse(VALID_SCHOOL_ID, null);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.modifySchool(updateDto, VALID_SCHOOL_ID),
                    "School name must not be null or empty"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated school name is empty")
        void modifySchool_Failure_WhenSchoolNameIsEmpty() {
            SchoolResponse updateDto = buildSchoolResponse(VALID_SCHOOL_ID, EMPTY_SCHOOL_NAME);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.modifySchool(updateDto, VALID_SCHOOL_ID),
                    "School name must not be null or empty"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated school name is blank/whitespace-only")
        void modifySchool_Failure_WhenSchoolNameIsBlank() {
            SchoolResponse updateDto = buildSchoolResponse(VALID_SCHOOL_ID, BLANK_SCHOOL_NAME);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> schoolService.modifySchool(updateDto, VALID_SCHOOL_ID),
                    "School name must not be null or empty"
            );
        }
    }
}