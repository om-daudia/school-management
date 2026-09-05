package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.DivisionRequest;
import com.odschool.dtos.DivisionResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
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
@DisplayName("DivisionService Unit Tests")
class DivisionServiceTest {

    private static final int VALID_STANDARD_ID = 1;
    private static final int NON_EXISTENT_STANDARD_ID = 888;
    private static final int NEGATIVE_STANDARD_ID = -1;

    private static final int VALID_DIVISION_ID = 10;
    private static final int NON_EXISTENT_DIVISION_ID = 999;
    private static final int NEGATIVE_DIVISION_ID = -1;

    private static final char VALID_DIVISION_VALUE = 'A';
    private static final char UPDATED_DIVISION_VALUE = 'B';
    private static final char LOWERCASE_DIVISION_VALUE = 'a';
    private static final char UNINITIALIZED_DIVISION_VALUE = '\u0000';

    @Mock
    private DivisionRepository divisionRepository;

    @Mock
    private StandardRepository standardRepository;

    @Mock
    private MapInterface mapInterface;

    @InjectMocks
    private DivisionService divisionService;

    private StandardEntity standardEntity;
    private DivisionEntity divisionEntity;
    private DivisionResponse divisionResponse;

    @BeforeEach
    void setUp() {
        standardEntity = buildStandardEntity(VALID_STANDARD_ID);
        divisionEntity = buildDivisionEntity(VALID_DIVISION_ID, VALID_DIVISION_VALUE, standardEntity);
        divisionResponse = buildDivisionResponse(VALID_DIVISION_ID, VALID_DIVISION_VALUE, VALID_STANDARD_ID);
    }

    // ==========================================================================================
    // Common / reusable helper methods
    // ==========================================================================================

    private StandardEntity buildStandardEntity(int id) {
        StandardEntity entity = new StandardEntity();
        entity.setId(id);
        return entity;
    }

    private DivisionEntity buildDivisionEntity(int id, char division, StandardEntity standardEntity) {
        DivisionEntity entity = new DivisionEntity();
        entity.setId(id);
        entity.setDivision(division);
        entity.setStandardEntity(standardEntity);
        return entity;
    }

    private DivisionResponse buildDivisionResponse(int id, char division, int standardId) {
        DivisionResponse response = new DivisionResponse();
        response.setId(id);
        response.setDivision(division);
        response.setStandardId(standardId);
        return response;
    }

    private DivisionRequest buildDivisionRequest(char division) {
        DivisionRequest request = new DivisionRequest();
        request.setDivision(division);
        return request;
    }

    private void mockSuccessfulEntityToResponseMapping(DivisionEntity entity, DivisionResponse response) {
        when(mapInterface.toDivisionResponse(entity)).thenReturn(response);
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
        verifyNoInteractions(divisionRepository);
        verifyNoInteractions(standardRepository);
    }

    // ==========================================================================================
    // getAllDivisions()
    // ==========================================================================================

    @Nested
    @DisplayName("getAllDivisions()")
    class GetAllDivisionsTests {

        @Test
        @DisplayName("Should return full division list successfully when divisions exist for the standard")
        void getAllDivisions_Success_ReturnsPopulatedDivisionList() {
            DivisionEntity secondEntity = buildDivisionEntity(11, UPDATED_DIVISION_VALUE, standardEntity);
            DivisionResponse secondResponse = buildDivisionResponse(11, UPDATED_DIVISION_VALUE, VALID_STANDARD_ID);

            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID))
                    .thenReturn(Arrays.asList(divisionEntity, secondEntity));
            when(mapInterface.toDivisionResponse(divisionEntity)).thenReturn(divisionResponse);
            when(mapInterface.toDivisionResponse(secondEntity)).thenReturn(secondResponse);

            ResponseEntity<Object> result = divisionService.getAllDivisions(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertEquals(2, data.size());

            verify(divisionRepository, times(1)).findAllByStandardEntityId(VALID_STANDARD_ID);
            verify(mapInterface, times(1)).toDivisionResponse(divisionEntity);
            verify(mapInterface, times(1)).toDivisionResponse(secondEntity);
        }

        @Test
        @DisplayName("Should return an empty list successfully when the standard has no divisions")
        void getAllDivisions_Success_ReturnsEmptyList_WhenNoneExist() {
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID)).thenReturn(new ArrayList<>());

            ResponseEntity<Object> result = divisionService.getAllDivisions(VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            List<?> data = (List<?>) apiResponse.getData();
            assertTrue(data.isEmpty());

            verify(mapInterface, never()).toDivisionResponse(any(DivisionEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standardId is negative, without querying either repository")
        void getAllDivisions_Failure_WhenStandardIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.getAllDivisions(NEGATIVE_STANDARD_ID),
                    "Standard id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should propagate exception when repository throws an unexpected error")
        void getAllDivisions_Failure_WhenRepositoryThrowsException() {
            when(divisionRepository.findAllByStandardEntityId(VALID_STANDARD_ID))
                    .thenThrow(new RuntimeException("Database unavailable"));

            assertThrows(RuntimeException.class, () -> divisionService.getAllDivisions(VALID_STANDARD_ID));

            verify(mapInterface, never()).toDivisionResponse(any(DivisionEntity.class));
        }
    }

    // ==========================================================================================
    // addDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("addDivision()")
    class AddDivisionTests {

        @Test
        @DisplayName("Should add a new division successfully when it does not already exist for the standard")
        void addDivision_Success_WhenDivisionIsUnique() {
            DivisionRequest request = buildDivisionRequest(VALID_DIVISION_VALUE);

            when(divisionRepository.findByDivisionAndStandardEntity_Id(VALID_DIVISION_VALUE, VALID_STANDARD_ID))
                    .thenReturn(null);
            when(standardRepository.findById(VALID_STANDARD_ID)).thenReturn(Optional.of(standardEntity));
            when(mapInterface.toDivisionEntity(request)).thenReturn(divisionEntity);
            when(mapInterface.toDivisionResponse(divisionEntity)).thenReturn(divisionResponse);

            ResponseEntity<Object> result = divisionService.addDivision(request, VALID_STANDARD_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals("new divisionId added", ((ApiResponse) result.getBody()).getMessage());
            assertEquals(standardEntity, divisionEntity.getStandardEntity());

            verify(divisionRepository, times(1)).findByDivisionAndStandardEntity_Id(VALID_DIVISION_VALUE, VALID_STANDARD_ID);
            verify(standardRepository, times(1)).findById(VALID_STANDARD_ID);
            verify(divisionRepository, times(1)).save(divisionEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when the parent standard does not exist")
        void addDivision_Failure_WhenStandardNotFound() {
            DivisionRequest request = buildDivisionRequest(VALID_DIVISION_VALUE);

            when(divisionRepository.findByDivisionAndStandardEntity_Id(VALID_DIVISION_VALUE, NON_EXISTENT_STANDARD_ID))
                    .thenReturn(null);
            when(standardRepository.findById(NON_EXISTENT_STANDARD_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> divisionService.addDivision(request, NON_EXISTENT_STANDARD_ID),
                    "Standard not found with id=" + NON_EXISTENT_STANDARD_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(divisionRepository, never()).save(any(DivisionEntity.class));
        }

        @Test
        @DisplayName("Should throw CONFLICT ApiException when the division already exists for the standard")
        void addDivision_Failure_WhenDivisionAlreadyExists() {
            DivisionRequest request = buildDivisionRequest(VALID_DIVISION_VALUE);

            when(divisionRepository.findByDivisionAndStandardEntity_Id(VALID_DIVISION_VALUE, VALID_STANDARD_ID))
                    .thenReturn(divisionEntity);

            assertApiExceptionThrown(
                    () -> divisionService.addDivision(request, VALID_STANDARD_ID),
                    "Division already exist",
                    HttpStatus.CONFLICT
            );

            verify(standardRepository, never()).findById(anyInt());
            verify(divisionRepository, never()).save(any(DivisionEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when standardId is negative, without any repository interaction")
        void addDivision_Failure_WhenStandardIdIsNegative() {
            DivisionRequest request = buildDivisionRequest(VALID_DIVISION_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.addDivision(request, NEGATIVE_STANDARD_ID),
                    "Standard id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when DivisionRequest is null")
        void addDivision_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.addDivision(null, VALID_STANDARD_ID),
                    "Division request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when division value is a lowercase letter")
        void addDivision_Failure_WhenDivisionValueIsLowercase() {
            DivisionRequest request = buildDivisionRequest(LOWERCASE_DIVISION_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.addDivision(request, VALID_STANDARD_ID),
                    "Division must be an uppercase letter between A and Z"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when division value is uninitialized")
        void addDivision_Failure_WhenDivisionValueIsUninitialized() {
            DivisionRequest request = buildDivisionRequest(UNINITIALIZED_DIVISION_VALUE);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.addDivision(request, VALID_STANDARD_ID),
                    "Division must be an uppercase letter between A and Z"
            );
        }
    }

    // ==========================================================================================
    // getDivisionById()
    // ==========================================================================================

    @Nested
    @DisplayName("getDivisionById()")
    class GetDivisionByIdTests {

        @Test
        @DisplayName("Should return division successfully when a valid division id is provided")
        void getDivisionById_Success_WhenDivisionExists() {
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            mockSuccessfulEntityToResponseMapping(divisionEntity, divisionResponse);

            ResponseEntity<Object> result = divisionService.getDivisionById(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals(divisionResponse, apiResponse.getData());
            verify(divisionRepository, times(1)).findById(VALID_DIVISION_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when division id does not exist")
        void getDivisionById_Failure_WhenDivisionNotFound() {
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> divisionService.getDivisionById(NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(mapInterface, never()).toDivisionResponse(any(DivisionEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when division id is negative, without querying the repository")
        void getDivisionById_Failure_WhenDivisionIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.getDivisionById(NEGATIVE_DIVISION_ID),
                    "Division id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // deleteDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("deleteDivision()")
    class DeleteDivisionTests {

        @Test
        @DisplayName("Should delete division successfully when a valid division id is provided")
        void deleteDivision_Success_WhenDivisionExists() {
            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            doNothing().when(divisionRepository).deleteById(VALID_DIVISION_ID);

            ResponseEntity<Object> result = divisionService.deleteDivision(VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            ApiResponse apiResponse = (ApiResponse) result.getBody();
            assertEquals("Division Deleted", apiResponse.getData());
            assertEquals("successful", apiResponse.getMessage());

            verify(divisionRepository, times(1)).findById(VALID_DIVISION_ID);
            verify(divisionRepository, times(1)).deleteById(VALID_DIVISION_ID);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when attempting to delete a non-existent division")
        void deleteDivision_Failure_WhenDivisionNotFound() {
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> divisionService.deleteDivision(NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(divisionRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when division id is negative, without any repository interaction")
        void deleteDivision_Failure_WhenDivisionIdIsNegative() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.deleteDivision(NEGATIVE_DIVISION_ID),
                    "Division id must be a positive number"
            );
        }
    }

    // ==========================================================================================
    // modifyDivision()
    // ==========================================================================================

    @Nested
    @DisplayName("modifyDivision()")
    class ModifyDivisionTests {

        @Test
        @DisplayName("Should modify division successfully when a valid id and request are provided")
        void modifyDivision_Success_WhenDivisionExists() {
            DivisionResponse request = buildDivisionResponse(VALID_DIVISION_ID, UPDATED_DIVISION_VALUE, VALID_STANDARD_ID);
            DivisionResponse updatedResponse = buildDivisionResponse(VALID_DIVISION_ID, UPDATED_DIVISION_VALUE, VALID_STANDARD_ID);

            when(divisionRepository.findById(VALID_DIVISION_ID)).thenReturn(Optional.of(divisionEntity));
            when(mapInterface.toDivisionResponse(divisionEntity)).thenReturn(updatedResponse);

            ResponseEntity<Object> result = divisionService.modifyDivision(request, VALID_DIVISION_ID);

            assertSuccessfulApiResponse(result, HttpStatus.OK);
            assertEquals(UPDATED_DIVISION_VALUE, divisionEntity.getDivision());

            verify(divisionRepository, times(1)).findById(VALID_DIVISION_ID);
            verify(divisionRepository, times(1)).save(divisionEntity);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND ApiException when modifying a non-existent division")
        void modifyDivision_Failure_WhenDivisionNotFound() {
            DivisionResponse request = buildDivisionResponse(NON_EXISTENT_DIVISION_ID, UPDATED_DIVISION_VALUE, VALID_STANDARD_ID);
            when(divisionRepository.findById(NON_EXISTENT_DIVISION_ID)).thenReturn(Optional.empty());

            assertApiExceptionThrown(
                    () -> divisionService.modifyDivision(request, NON_EXISTENT_DIVISION_ID),
                    "Division not found with id=" + NON_EXISTENT_DIVISION_ID,
                    HttpStatus.NOT_FOUND
            );

            verify(divisionRepository, never()).save(any(DivisionEntity.class));
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when division id is negative, without any repository interaction")
        void modifyDivision_Failure_WhenDivisionIdIsNegative() {
            DivisionResponse request = buildDivisionResponse(VALID_DIVISION_ID, UPDATED_DIVISION_VALUE, VALID_STANDARD_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.modifyDivision(request, NEGATIVE_DIVISION_ID),
                    "Division id must be a positive number"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when DivisionResponse dto is null")
        void modifyDivision_Failure_WhenRequestIsNull() {
            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.modifyDivision(null, VALID_DIVISION_ID),
                    "Division request must not be null"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated division value is a lowercase letter")
        void modifyDivision_Failure_WhenDivisionValueIsLowercase() {
            DivisionResponse request = buildDivisionResponse(VALID_DIVISION_ID, LOWERCASE_DIVISION_VALUE, VALID_STANDARD_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.modifyDivision(request, VALID_DIVISION_ID),
                    "Division must be an uppercase letter between A and Z"
            );
        }

        @Test
        @DisplayName("Should throw BAD_REQUEST ApiException when updated division value is uninitialized")
        void modifyDivision_Failure_WhenDivisionValueIsUninitialized() {
            DivisionResponse request = buildDivisionResponse(VALID_DIVISION_ID, UNINITIALIZED_DIVISION_VALUE, VALID_STANDARD_ID);

            assertValidationFailureWithNoRepositoryInteraction(
                    () -> divisionService.modifyDivision(request, VALID_DIVISION_ID),
                    "Division must be an uppercase letter between A and Z"
            );
        }
    }
}
