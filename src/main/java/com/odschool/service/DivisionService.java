package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.DivisionRequest;
import com.odschool.dtos.DivisionResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
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
public class DivisionService {
    @Autowired
    DivisionRepository divisionRepository;
    @Autowired
    StandardRepository standardRepository;
    @Autowired
    MapInterface mapInterface;

    public ResponseEntity<Object> getAllDivisions(int standardId) {
        log.info("[SERVICE] Start fetching all divisions");
        List<DivisionResponse> divisionList = divisionRepository.findAllByStandardEntityId(standardId).stream()
                .map(mapInterface::toDivisionResponse)
                .collect(Collectors.toList());

        ApiResponse response = new ApiResponse(divisionList, "Division list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addDivision(DivisionRequest divisionRequest, int standardId) {
        log.trace("[SERVICE] Start adding new Division: {}", divisionRequest.getDivision());
        try {
            DivisionEntity findDivision = divisionRepository.findByDivisionAndStandardEntity_Id(
                    divisionRequest.getDivision(), standardId);

            if (findDivision == null) {
                StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
                if (standardEntity == null) {
                    ApiResponse response = new ApiResponse(
                            "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
                    );
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }

                DivisionEntity divisionEntity = mapInterface.toDivisionEntity(divisionRequest);
                divisionEntity.setStandardEntity(standardEntity);
                divisionRepository.save(divisionEntity);

                ApiResponse response = new ApiResponse(
                        mapInterface.toDivisionResponse(divisionEntity),
                        "new divisionId added", true, HttpStatus.OK.value()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse response = new ApiResponse(
                        "DIVISION_EXIST_ERROR", "Division already exist", false, HttpStatus.CONFLICT.value()
                );
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while adding divisionId", e);
            ApiResponse response = new ApiResponse(
                    "DIVISION_UNEXPECTED_ERROR", "Division unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getDivisionById(int divisionId) {
        try {
            DivisionEntity findDivision = divisionRepository.findById(divisionId).orElse(null);
            if (findDivision == null) {
                ApiResponse response = new ApiResponse(
                        "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            ApiResponse response = new ApiResponse(
                    mapInterface.toDivisionResponse(findDivision), "divisionId found successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while fetching divisionId with divisionId: {}", divisionId, e);
            ApiResponse response = new ApiResponse(
                    "DIVISION_UNEXPECTED_ERROR", "Division unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> deleteDivision(int divisionId) {
        try {
            DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
            if (divisionEntity == null) {
                ApiResponse response = new ApiResponse(
                        "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            divisionRepository.deleteById(divisionEntity.getId());
            ApiResponse response = new ApiResponse("Division Deleted", "successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while deleting divisionId with divisionId: {}", divisionId, e);
            ApiResponse response = new ApiResponse(
                    "DIVISION_UNEXPECTED_ERROR", "Division unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> modifyDivision(DivisionResponse divisionDto, int divisionId) {
        try {
            DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
            if (divisionEntity == null) {
                ApiResponse response = new ApiResponse(
                        "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            divisionEntity.setDivision(divisionDto.getDivision());
            divisionRepository.save(divisionEntity);

            ApiResponse response = new ApiResponse(
                    mapInterface.toDivisionResponse(divisionEntity), "successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while modifying divisionId with divisionId: {}", divisionId, e);
            ApiResponse response = new ApiResponse(
                    "DIVISION_UNEXPECTED_ERROR", "Division unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}