package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.StandardRequest;
import com.odschool.dtos.StandardResponse;
import com.odschool.entity.SchoolEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.SchoolRepository;
import com.odschool.repository.StandardRepository;
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
@DisplayName("StandardService Unit Tests")
class StandardServiceTest {

    private static final int VALID_SCHOOL_ID = 1;
    private static final int NON_EXISTENT_SCHOOL_ID = 888;
    private static final int NEGATIVE_SCHOOL_ID = -1;

    private static final int VALID_STANDARD_ID = 10;
    private static final int NON_EXISTENT_STANDARD_ID = 999;
    private static final int NEGATIVE_STANDARD_ID = -1;

    private static final int VALID_STANDARD_VALUE = 5;
    private static final int UPDATED_STANDARD_VALUE = 6;
    private static final int NEGATIVE_STANDARD_VALUE = -3;
    private static final int ZERO_STANDARD_VALUE = 0;

    private static final String VALID_SCHOOL_NAME = "Springfield Elementary";

    @Mock
    private StandardRepository standardRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private StandardService standardService;

    private SchoolEntity schoolEntity;
    private StandardEntity standardEntity;
    private StandardResponse standardResponse;

    @BeforeEach
    void setUp() {
        schoolEntity = buildSchoolEntity(VALID_SCHOOL_ID, VALID_SCHOOL_NAME);
        standardEntity = buildStandardEntity(VALID_STANDARD_ID, VALID_STANDARD_VALUE, schoolEntity);
        standardResponse = buildStandardResponse(VALID_STANDARD_ID, VALID_STANDARD_VALUE, VALID_SCHOOL_ID);
    }

    // ==========================================================================================
    // Common / reusable helper methods
    // ==========================================================================================

    private SchoolEntity buildSchoolEntity(int id, String schoolName) {
        SchoolEntity entity = new SchoolEntity(schoolName);
        entity.setId(id);
        return entity;
    }

    private StandardEntity buildStandardEntity(int id, int standard, SchoolEntity schoolEntity) {
        StandardEntity entity = new StandardEntity();
        entity.setId(id);
        entity.setStandard(standard);
        entity.setSchoolEntity(schoolEntity);
        return entity;
    }

    private StandardResponse buildStandardResponse(int id, int standard, int schoolId) {
        StandardResponse response = new StandardResponse();
        response.setId(id);
        response.setStandard(standard);
        response.setSchoolId(schoolId);
        return response;
    }

    private StandardRequest buildStandardRequest(int standard) {
        StandardRequest request = new StandardRequest();
        request.setStandard(standard);
        return request;
    }

    private void mockSuccessfulEntityToResponseMapping(StandardEntity entity, StandardResponse response) {
        when(mapInterface.toStandardResponse(entity)).thenReturn(response);
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
        verifyNoInteractions(standardRepository);
        verifyNoInteractions(schoolRepository);
    }

    // ==========================================================================================
    // getAllStandards()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllStandards()")
    class GetAllStandardsTests {

        @Test
        @DisplayName("Should return full standard list successfully when standards exist for the school")
        void getAllStandards_Success_ReturnsPopulatedStandardList() {
            StandardEntity secondEntity = buildStandardEntity(11, 6, schoolEntity);
            StandardResponse secondResponse = buildStandardResponse(11, 6, VALID_SCHOOL_ID);

            when(standardRepository.findAllBySchoolEntity_Id(VALID_SCHOOL_ID))
                    .thenReturn(Arrays.asList(standardEntity, secondEntity));
            when(mapInterface.toStandardResponse(standardEntity)).thenReturn(standardResponse);
            when(mapInterface.toStandardResponse(secondEntity)).thenReturn(secondResponse);

            ResponseEntity<Object> result = standardService.getAllStandards(VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size());

            verify(standardRepository, times(1)).findAllBySchoolEntity_Id(VALID_SCHOOL_ID);
            verify(mapInterface, times(1)).toStandardResponse(standardEntity);
            verify(mapInterface, times(1)).toStandardResponse(secondEntity);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the school has no standards")
        void getAllStandards_Success_ReturnsEmptyList_WhenNoneExist() {
            when(standardRepository.findAllBySchoolEntity_Id(VALID_SCHOOL_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = standardService.getAllStandards(VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toStandardResponse(any(StandardEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when schoolId is negative, without querying either repository")
        void getAllStandards_Failure_WhenSchoolIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.getAllStandards(NEGATIVE_SCHOOL_ID),
                    "School id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should propagate exception when repository throws an unexpected error")
        void getAllStandards_Failure_WhenRepositoryThrowsException() {
            when(standardRepository.findAllBySchoolEntity_Id(VALID_SCHOOL_ID))
                    .thenThrow(new RuntimeException("Database unavailable"));

            assertThrows(RuntimeException.class, () -> standardService.getAllStandards(VALID_SCHOOL_ID));

            verify(mapInterface, never()).toStandardResponse(any(StandardEntity.class));
        }
    }

    // ==========================================================================================
    // addStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("addStandard()")
    class AddStandardTests {

        @Test
        @DisplayName("Should add a new standard successfully when it does not already exist for the school")
        void addStandard_Success_WhenStandardIsUnique() {
            StandardRequest request = buildStandardRequest(VALID_STANDARD_VALUE);

            when(standardRepository.findByStandardAndSchoolEntity_Id(VALID_STANDARD_VALUE, VALID_SCHOOL_ID))
                    .thenReturn(null);
            when(schoolRepository.findById(VALID_SCHOOL_ID)).thenReturn(Optional.of(schoolEntity));
            when(mapInterface.toStandardEntity(request)).thenReturn(standardEntity);
            when(standardRepository.save(standardEntity)).thenReturn(standardEntity);
            when(mapInterface.toStandardResponse(standardEntity)).thenReturn(standardResponse);

            ResponseEntity<Object> result = standardService.addStandard(request, VALID_SCHOOL_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("new standardId added", ((ApiResponse) result.getBody()).getMessage());
            assertEquals(schoolEntity, standardEntity.getSchoolEntity());

            verify(standardRepository, times(1)).findByStandardAndSchoolEntity_Id(VALID_STANDARD_VALUE, VALID_SCHOOL_ID);
            verify(schoolRepository, times(1)).findById(VALID_SCHOOL_ID);
            verify(standardRepository, times(1)).save(standardEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the parent school does not exist")
        void addStandard_Failure_WhenSchoolNotFound() {
            StandardRequest request = buildStandardRequest(VALID_STANDARD_VALUE);

            when(standardRepository.findByStandardAndSchoolEntity_Id(VALID_STANDARD_VALUE, NON_EXISTENT_SCHOOL_ID))
                    .thenReturn(null);
            when(schoolRepository.findById(NON_EXISTENT_SCHOOL_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> standardService.addStandard(request, NON_EXISTENT_SCHOOL_ID),
                    "School not found with id=" + NON_EXISTENT_SCHOOL_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(standardRepository, never()).save(any(StandardEntity.class));
        }

        @Test
        @DisplayName("Should throw CONFLICT ApiException when the standard already exists for the school")
        void addStandard_Failure_WhenStandardAlreadyExists() {
            StandardRequest request = buildStandardRequest(VALID_STANDARD_VALUE);

            when(standardRepository.findByStandardAndSchoolEntity_Id(VALID_STANDARD_VALUE, VALID_SCHOOL_ID))
                    .thenReturn(standardEntity);

            assertApiExceptionThrown(
                    () -> standardService.addStandard(request, VALID_SCHOOL_ID),
                    "Standard already exist",
                    HttpStatus.CONFLICT
            );

            verify(schoolRepository, never()).findById(anyInt());
            verify(standardRepository, never()).save(any(StandardEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when schoolId is negative, without any repository interaction")
        void addStandard_Failure_WhenSchoolIdIsNegative() {
            StandardRequest request = buildStandardRequest(VALID_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.addStandard(request, NEGATIVE_SCHOOL_ID),
                    "School id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when StandardRequest is null")
        void addStandard_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.addStandard(null, VALID_SCHOOL_ID),
                    "Standard request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standard value is negative")
        void addStandard_Failure_WhenStandardValueIsNegative() {
            StandardRequest request = buildStandardRequest(NEGATIVE_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.addStandard(request, VALID_SCHOOL_ID),
                    "Standard must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standard value is zero")
        void addStandard_Failure_WhenStandardValueIsZero() {
            StandardRequest request = buildStandardRequest(ZERO_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.addStandard(request, VALID_SCHOOL_ID),
                    "Standard must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // getStandardById()
    // ==========================================================================================

    @Nested
    @DisplayName("getStandardById()")
    class GetStandardByIdTests {

        @Test
        @DisplayName("Should return standard successfully when a valid standard id is provided")
        void getStandardById_Success_WhenStandardExists() {
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            mockSuccessfulEntityToResponseMapping(standardEntity, standardResponse);

            ResponseEntity<Object> result = standardService.getStandardById(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals(standardResponse, apiResponse.getData());
            verify(standardRepository, times(1)).findById(VALID_STANDARD_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when standard id does not exist")
        void getStandardById_Failure_WhenStandardNotFound() {
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> standardService.getStandardById(NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(mapInterface, never()).toStandardResponse(any(StandardEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standard id is negative, without querying the repository")
        void getStandardById_Failure_WhenStandardIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.getStandardById(NEGATIVE_STANDARD_ID),
                    "Standard id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // deleteStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("deleteStandard()")
    class DeleteStandardTests {

        @Test
        @DisplayName("Should delete standard successfully when a valid standard id is provided")
        void deleteStandard_Success_WhenStandardExists() {
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            doNothing().when(standardRepository).deleteById(VALID_STANDARD_ID);

            ResponseEntity<Object> result = standardService.deleteStandard(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals("Standard Deleted", apiResponse.getData());
            assertEquals("successful", apiResponse.getMessage());

            verify(standardRepository, times(1)).findById(VALID_STANDARD_ID);
            verify(standardRepository, times(1)).deleteById(VALID_STANDARD_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when attempting to delete a non-existent standard")
        void deleteStandard_Failure_WhenStandardNotFound() {
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> standardService.deleteStandard(NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(standardRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standard id is negative, without any repository interaction")
        void deleteStandard_Failure_WhenStandardIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.deleteStandard(NEGATIVE_STANDARD_ID),
                    "Standard id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // modifyStandard()
    // ==========================================================================================

    @Nested
    @DisplayName("modifyStandard()")
    class ModifyStandardTests {

        @Test
        @DisplayName("Should modify standard successfully when a valid id and request are provided")
        void modifyStandard_Success_WhenStandardExists() {
            StandardRequest request = buildStandardRequest(UPDATED_STANDARD_VALUE);
            StandardResponse updatedResponse = buildStandardResponse(VALID_STANDARD_ID, UPDATED_STANDARD_VALUE, VALID_SCHOOL_ID);

            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(mapInterface.toStandardResponse(standardEntity)).thenReturn(updatedResponse);

            ResponseEntity<Object> result = standardService.modifyStandard(request, VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_STANDARD_VALUE, standardEntity.getStandard());

            verify(standardRepository, times(1)).findById(VALID_STANDARD_ID);
            verify(standardRepository, times(1)).save(standardEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when modifying a non-existent standard")
        void modifyStandard_Failure_WhenStandardNotFound() {
            StandardRequest request = buildStandardRequest(UPDATED_STANDARD_VALUE);
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> standardService.modifyStandard(request, NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(standardRepository, never()).save(any(StandardEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standard id is negative, without any repository interaction")
        void modifyStandard_Failure_WhenStandardIdIsNegative() {
            StandardRequest request = buildStandardRequest(UPDATED_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.modifyStandard(request, NEGATIVE_STANDARD_ID),
                    "Standard id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when StandardRequest is null")
        void modifyStandard_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.modifyStandard(null, VALID_STANDARD_ID),
                    "Standard request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated standard value is negative")
        void modifyStandard_Failure_WhenStandardValueIsNegative() {
            StandardRequest request = buildStandardRequest(NEGATIVE_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.modifyStandard(request, VALID_STANDARD_ID),
                    "Standard must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated standard value is zero")
        void modifyStandard_Failure_WhenStandardValueIsZero() {
            StandardRequest request = buildStandardRequest(ZERO_STANDARD_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> standardService.modifyStandard(request, VALID_STANDARD_ID),
                    "Standard must be a positive number"
            );
        }
    }
}
