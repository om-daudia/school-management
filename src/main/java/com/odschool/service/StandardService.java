package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.StandardRequest;
import com.odschool.dtos.StandardResponse;
import com.odschool.entity.SchoolEntity;
import com.odschool.entity.StandardEntity;
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
        log.info("[SERVICE] Start fetching all standards");
        List<StandardResponse> standardList = standardRepository.findAllBySchoolEntity_Id(schoolId).stream()
                .map(mapInterface::toStandardResponse)
                .collect(Collectors.toList());

        log.info("[SERVICE] Fetched {} standards", standardList.size());
        ApiResponse response = new ApiResponse(standardList, "Standard list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addStandard(StandardRequest standardRequest, int schoolId) {
        log.trace("[SERVICE] Start adding new Standard: {}", standardRequest.getStandard());
        try {
            StandardEntity findStandard = standardRepository.findByStandardAndSchoolEntity_Id(
                    standardRequest.getStandard(), schoolId);

            if (findStandard == null) {
                SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);
                if (schoolEntity == null) {
                    log.warn("[SERVICE] school not found with schoolId: {}", schoolId);
                    ApiResponse response = new ApiResponse(
                            "SCHOOL_NOT_FOUND_ERROR", "School not found", false, HttpStatus.NOT_FOUND.value()
                    );
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }

                StandardEntity standardEntity = mapInterface.toStandardEntity(standardRequest);
                standardEntity.setSchoolEntity(schoolEntity);
                StandardEntity resp = standardRepository.save(standardEntity);

                log.info("[SERVICE] standardId add successful");
                ApiResponse response = new ApiResponse(
                        mapInterface.toStandardResponse(resp),
                        "new standardId added", true, HttpStatus.OK.value()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                log.info("[SERVICE] standardId already exists for this school");
                ApiResponse response = new ApiResponse(
                        "STANDARD_EXIST_ERROR", "Standard already exist", false, HttpStatus.CONFLICT.value()
                );
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while adding standardId", e);
            ApiResponse response = new ApiResponse(
                    "STANDARD_UNEXPECTED_ERROR", "Standard unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getStandardById(int standardId) {
        log.trace("[SERVICE] Start fetching standardId with standardId: {}", standardId);
        try {
            StandardEntity findStandard = standardRepository.findById(standardId).orElse(null);
            if (findStandard == null) {
                log.warn("[SERVICE] standardId not found with standardId: {}", standardId);
                ApiResponse response = new ApiResponse(
                        "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            StandardResponse standardResponse = mapInterface.toStandardResponse(findStandard);
            ApiResponse response = new ApiResponse(standardResponse, "standardId found successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while fetching standardId with standardId: {}", standardId, e);
            ApiResponse response = new ApiResponse(
                    "STANDARD_UNEXPECTED_ERROR", "Standard unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> deleteStandard(int standardId) {
        log.trace("[SERVICE] Start deleteStandard method with standardId {}", standardId);
        try {
            StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
            if (standardEntity == null) {
                log.warn("[SERVICE] standardId not found with standardId: {}", standardId);
                ApiResponse response = new ApiResponse(
                        "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            standardRepository.deleteById(standardEntity.getId());
            log.info("[SERVICE] standardId deleted successful with standardId {}", standardEntity.getId());

            ApiResponse response = new ApiResponse("Standard Deleted", "successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while deleting standardId with standardId: {}", standardId, e);
            ApiResponse response = new ApiResponse(
                    "STANDARD_UNEXPECTED_ERROR", "Standard unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> modifyStandard(StandardRequest standardRequest, int standardId) {
        log.trace("[SERVICE] Start modifyStandard with standardDTO: {} and standardId: {}", standardRequest, standardId);
        try {
            StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
            if (standardEntity == null) {
                log.warn("[SERVICE] standardId not found with standardId: {}", standardId);
                ApiResponse response = new ApiResponse(
                        "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            standardEntity.setStandard(standardRequest.getStandard());
            standardRepository.save(standardEntity);
            log.info("[SERVICE] update successful with standardId: {}", standardEntity.getId());

            ApiResponse response = new ApiResponse(
                    mapInterface.toStandardResponse(standardEntity), "successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while modifying standardId with standardId: {}", standardId, e);
            ApiResponse response = new ApiResponse(
                    "STANDARD_UNEXPECTED_ERROR", "Standard unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
