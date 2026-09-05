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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StandardService {
    @Autowired
    StandardRepository standardRepository;
    @Autowired
    SchoolRepository schoolRepository;
    @Autowired
    MapInterface mapInterface;

    public ResponseEntity<Object> getAllStandards(int schoolId) {
        log.info("[ODSCHOOL][StandardService] Start fetching all standards for schoolId: {}", schoolId);

        if (schoolId <= 0) {
            log.info("[ODSCHOOL][StandardService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }

        List<StandardResponse> standardList = standardRepository.findAllBySchoolEntity_Id(schoolId).stream()
                .map(mapInterface::toStandardResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][StandardService] Fetched {} standards successfully for schoolId: {}", standardList.size(), schoolId);

        ApiResponse response = new ApiResponse(standardList, "Standard list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addStandard(StandardRequest standardRequest, int schoolId) {
        log.info("[ODSCHOOL][StandardService] Start adding new Standard for schoolId: {}", schoolId);

        if (schoolId <= 0) {
            log.info("[ODSCHOOL][StandardService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }
        validateStandardRequest(standardRequest);

        log.info("[ODSCHOOL][StandardService] Validated request, standard: {} for schoolId: {}",
                standardRequest.getStandard(), schoolId);

        StandardEntity findStandard = standardRepository.findByStandardAndSchoolEntity_Id(
                standardRequest.getStandard(), schoolId);

        if (findStandard == null) {
            SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);
            if (schoolEntity == null) {
                log.error("[ODSCHOOL][StandardService] Error while adding standard - School not found with id={}", schoolId);
                throw new ApiException("School not found with id=" + schoolId, HttpStatus.NOT_FOUND);
            }

            StandardEntity standardEntity = mapInterface.toStandardEntity(standardRequest);
            standardEntity.setSchoolEntity(schoolEntity);
            StandardEntity resp = standardRepository.save(standardEntity);

            log.info("[ODSCHOOL][StandardService] Standard added successfully with standardId: {} and standard: {} for schoolId: {}",
                    resp.getId(), resp.getStandard(), schoolId);

            ApiResponse response = new ApiResponse(
                    mapInterface.toStandardResponse(resp),
                    "new standardId added", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            log.error("[ODSCHOOL][StandardService] Error while adding standard - standard: {} already exists for schoolId: {}",
                    standardRequest.getStandard(), schoolId);
            throw new ApiException("Standard already exist", HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getStandardById(int standardId) {
        log.info("[ODSCHOOL][StandardService] Start fetching standard with standardId: {}", standardId);

        validateStandardId(standardId);

        StandardEntity findStandard = standardRepository.findById(standardId).orElse(null);
        if (findStandard == null) {
            log.error("[ODSCHOOL][StandardService] Error while fetching standard - Standard not found with id={}", standardId);
            throw new ApiException("Standard not found with id=" + standardId, HttpStatus.NOT_FOUND);
        }

        StandardResponse standardResponse = mapInterface.toStandardResponse(findStandard);
        log.info("[ODSCHOOL][StandardService] Standard found successfully with standardId: {}", standardId);

        ApiResponse response = new ApiResponse(standardResponse, "standardId found successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> deleteStandard(int standardId) {
        log.info("[ODSCHOOL][StandardService] Start deleting standard with standardId: {}", standardId);

        validateStandardId(standardId);

        StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
        if (standardEntity == null) {
            log.error("[ODSCHOOL][StandardService] Error while deleting standard - Standard not found with id={}", standardId);
            throw new ApiException("Standard not found with id=" + standardId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][StandardService] Standard found with standardId: {}, proceeding to delete", standardEntity.getId());

        standardRepository.deleteById(standardEntity.getId());

        log.info("[ODSCHOOL][StandardService] Standard deleted successfully with standardId: {}", standardEntity.getId());

        ApiResponse response = new ApiResponse("Standard Deleted", "successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> modifyStandard(StandardRequest standardRequest, int standardId) {
        log.info("[ODSCHOOL][StandardService] Start modifying standard with standardId: {}", standardId);

        validateStandardId(standardId);
        validateStandardRequest(standardRequest);

        log.info("[ODSCHOOL][StandardService] Validated request, requested standard: {}", standardRequest.getStandard());

        StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
        if (standardEntity == null) {
            log.error("[ODSCHOOL][StandardService] Error while modifying standard - Standard not found with id={}", standardId);
            throw new ApiException("Standard not found with id=" + standardId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][StandardService] Old standard: {} for standardId: {}", standardEntity.getStandard(), standardId);

        standardEntity.setStandard(standardRequest.getStandard());

        log.info("[ODSCHOOL][StandardService] Updated standard: {} for standardId: {}", standardEntity.getStandard(), standardId);

        standardRepository.save(standardEntity);

        log.info("[ODSCHOOL][StandardService] Standard updated successfully with standardId: {}", standardEntity.getId());

        ApiResponse response = new ApiResponse(
                mapInterface.toStandardResponse(standardEntity), "successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateStandardRequest(StandardRequest standardRequest) {
        if (standardRequest == null) {
            log.info("[ODSCHOOL][StandardService] Validation failed: standardRequest is null");
            throw new ApiException("Standard request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (standardRequest.getStandard() <= 0) {
            log.info("[ODSCHOOL][StandardService] Validation failed: standard {} is not a positive integer",
                    standardRequest.getStandard());
            throw new ApiException("Standard must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }
    private void validateStandardId(int standardId) {
        if (standardId <= 0) {
            log.info("[ODSCHOOL][StandardService] Validation failed: standardId {} is not a positive integer", standardId);
            throw new ApiException("Standard id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }
}