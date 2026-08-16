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
        log.info("[ODSCHOOL][DivisionService] Start fetching all divisions for standardId: {}", standardId);

        List<DivisionResponse> divisionList = divisionRepository.findAllByStandardEntityId(standardId).stream()
                .map(mapInterface::toDivisionResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][DivisionService] Fetched {} divisions successfully for standardId: {}", divisionList.size(), standardId);

        ApiResponse response = new ApiResponse(divisionList, "Division list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addDivision(DivisionRequest divisionRequest, int standardId) {
        log.info("[ODSCHOOL][DivisionService] Start adding new Division: {} for standardId: {}",
                divisionRequest.getDivision(), standardId);

        DivisionEntity findDivision = divisionRepository.findByDivisionAndStandardEntity_Id(
                divisionRequest.getDivision(), standardId);

        if (findDivision == null) {
            StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
            if (standardEntity == null) {
                log.error("[ODSCHOOL][DivisionService] Error while adding division - Standard not found with id={}", standardId);
                throw new ApiException("Standard not found with id=" + standardId, HttpStatus.NOT_FOUND);
            }

            DivisionEntity divisionEntity = mapInterface.toDivisionEntity(divisionRequest);
            divisionEntity.setStandardEntity(standardEntity);
            divisionRepository.save(divisionEntity);

            log.info("[ODSCHOOL][DivisionService] Division added successfully with divisionId: {} and division: {} for standardId: {}",
                    divisionEntity.getId(), divisionEntity.getDivision(), standardId);

            ApiResponse response = new ApiResponse(
                    mapInterface.toDivisionResponse(divisionEntity),
                    "new divisionId added", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            log.error("[ODSCHOOL][DivisionService] Error while adding division - division: {} already exists for standardId: {}",
                    divisionRequest.getDivision(), standardId);
            throw new ApiException("Division already exist", HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getDivisionById(int divisionId) {
        log.info("[ODSCHOOL][DivisionService] Start fetching division with divisionId: {}", divisionId);

        DivisionEntity findDivision = divisionRepository.findById(divisionId).orElse(null);
        if (findDivision == null) {
            log.error("[ODSCHOOL][DivisionService] Error while fetching division - Division not found with id={}", divisionId);
            throw new ApiException("Division not found with id=" + divisionId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][DivisionService] Division found successfully with divisionId: {}", divisionId);

        ApiResponse response = new ApiResponse(
                mapInterface.toDivisionResponse(findDivision), "divisionId found successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> deleteDivision(int divisionId) {
        log.info("[ODSCHOOL][DivisionService] Start deleting division with divisionId: {}", divisionId);

        DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
        if (divisionEntity == null) {
            log.error("[ODSCHOOL][DivisionService] Error while deleting division - Division not found with id={}", divisionId);
            throw new ApiException("Division not found with id=" + divisionId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][DivisionService] Division found with divisionId: {}, proceeding to delete", divisionEntity.getId());

        divisionRepository.deleteById(divisionEntity.getId());

        log.info("[ODSCHOOL][DivisionService] Division deleted successfully with divisionId: {}", divisionEntity.getId());

        ApiResponse response = new ApiResponse("Division Deleted", "successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> modifyDivision(DivisionResponse divisionDto, int divisionId) {
        log.info("[ODSCHOOL][DivisionService] Start modifying division with divisionId: {} and requested division: {}",
                divisionId, divisionDto.getDivision());

        DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
        if (divisionEntity == null) {
            log.error("[ODSCHOOL][DivisionService] Error while modifying division - Division not found with id={}", divisionId);
            throw new ApiException("Division not found with id=" + divisionId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][DivisionService] Old division: {} for divisionId: {}", divisionEntity.getDivision(), divisionId);

        divisionEntity.setDivision(divisionDto.getDivision());

        log.info("[ODSCHOOL][DivisionService] Updated division: {} for divisionId: {}", divisionEntity.getDivision(), divisionId);

        divisionRepository.save(divisionEntity);

        log.info("[ODSCHOOL][DivisionService] Division updated successfully with divisionId: {}", divisionEntity.getId());

        ApiResponse response = new ApiResponse(
                mapInterface.toDivisionResponse(divisionEntity), "successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}