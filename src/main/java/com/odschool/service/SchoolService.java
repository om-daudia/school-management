package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SchoolRequest;
import com.odschool.dtos.SchoolResponse;
import com.odschool.entity.SchoolEntity;
import com.odschool.exception.ApiException;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.SchoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchoolService {
    @Autowired
    SchoolRepository schoolRepository;
    @Autowired
    MapInterface mapInterface;

    public ResponseEntity<Object> getAllSchools() {
        log.info("[ODSCHOOL][SchoolService] Start fetching all schools");

        List<SchoolResponse> schoolList = schoolRepository.findAll().stream()
                .map(mapInterface::toSchoolResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][SchoolService] Fetched {} schools successfully", schoolList.size());

        ApiResponse response = new ApiResponse(schoolList, "School list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public ResponseEntity<Object> addSchool(SchoolRequest schoolRequest) {
        log.info("[ODSCHOOL][SchoolService] Start adding new School");

        if (schoolRequest == null) {
            log.info("[ODSCHOOL][SchoolService] Validation failed: schoolRequest is null");
            throw new ApiException("School request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(schoolRequest.getSchoolName())) {
            log.info("[ODSCHOOL][SchoolService] Validation failed: schoolName is null/empty/blank");
            throw new ApiException("School name must not be null or empty", HttpStatus.BAD_REQUEST);
        }

        log.info("[ODSCHOOL][SchoolService] Validated request, school name: {}", schoolRequest.getSchoolName());

        String schoolName = schoolRequest.getSchoolName().trim();

        SchoolEntity findSchool = schoolRepository.findBySchoolName(schoolName);
        if (findSchool != null) {
            log.info("[ODSCHOOL][SchoolService] School already exists with school name: {}, sending CONFLICT response",
                    schoolName);
            throw new ApiException("School already exist", HttpStatus.CONFLICT);
        }

        SchoolEntity schoolEntity = new SchoolEntity(schoolName);
        schoolRepository.save(schoolEntity);

        log.info("[ODSCHOOL][SchoolService] School added successfully with schoolId: {} and school name: {}",
                schoolEntity.getId(), schoolEntity.getSchoolName());

        ApiResponse response = new ApiResponse(
                mapInterface.toSchoolResponse(schoolEntity),
                "new school added",
                true,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> getSchoolById(int schoolId) {
        log.info("[ODSCHOOL][SchoolService] Start fetching school with schoolId: {}", schoolId);

        if (schoolId <= 0) {
            log.info("[ODSCHOOL][SchoolService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }

        SchoolEntity findSchool = schoolRepository.findById(schoolId).orElse(null);

        if (findSchool == null) {
            log.info("[ODSCHOOL][SchoolService] School not found with schoolId: {}, error message: School not found with id={}",
                    schoolId, schoolId);
            throw new ApiException("School not found with id=" + schoolId, HttpStatus.OK);
        }

        SchoolResponse schoolResponse = mapInterface.toSchoolResponse(findSchool);
        log.info("[ODSCHOOL][SchoolService] School found successfully with schoolId: {}", schoolId);

        ApiResponse response = new ApiResponse(schoolResponse, "school found successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public ResponseEntity<Object> deleteSchool(int schoolId) {
        log.info("[ODSCHOOL][SchoolService] Start deleting school with schoolId: {}", schoolId);

        if (schoolId <= 0) {
            log.info("[ODSCHOOL][SchoolService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }

        SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);

        if (schoolEntity == null) {
            log.info("[ODSCHOOL][SchoolService] School not found with schoolId: {}, error message: School not found with id={}",
                    schoolId, schoolId);
            throw new ApiException("School not found with id=" + schoolId, HttpStatus.OK);
        }

        log.info("[ODSCHOOL][SchoolService] School found with schoolId: {}, proceeding to delete", schoolEntity.getId());

        schoolRepository.deleteById(schoolEntity.getId());

        log.info("[ODSCHOOL][SchoolService] School deleted successfully with schoolId: {}", schoolEntity.getId());

        ApiResponse response = new ApiResponse("School Deleted", "successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> modifySchool(SchoolResponse schoolDto, int schoolId) {
        log.info("[ODSCHOOL][SchoolService] Start modifying school with schoolId: {}", schoolId);

        if (schoolId <= 0) {
            log.info("[ODSCHOOL][SchoolService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }
            if (schoolDto == null) {
                log.info("[ODSCHOOL][SchoolService] Validation failed: schoolDto is null");
                throw new ApiException("School data must not be null", HttpStatus.BAD_REQUEST);
            }
            if (!StringUtils.hasText(schoolDto.getSchoolName())) {
                log.info("[ODSCHOOL][SchoolService] Validation failed: schoolName is null/empty/blank");
                throw new ApiException("School name must not be null or empty", HttpStatus.BAD_REQUEST);
            }


        log.info("[ODSCHOOL][SchoolService] Validated request, requested school name: {}", schoolDto.getSchoolName());

        SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);
        if (schoolEntity == null) {
            log.info("[ODSCHOOL][SchoolService] School not found with schoolId: {}, error message: School not found with id={}",
                    schoolId, schoolId);
            throw new ApiException("School not found with id=" + schoolId, HttpStatus.OK);
        }

        log.info("[ODSCHOOL][SchoolService] Old school name: {} for schoolId: {}", schoolEntity.getSchoolName(), schoolId);

        schoolEntity.setSchoolName(schoolDto.getSchoolName().trim());

        log.info("[ODSCHOOL][SchoolService] Updated school name: {} for schoolId: {}", schoolEntity.getSchoolName(), schoolId);

        schoolRepository.save(schoolEntity);

        log.info("[ODSCHOOL][SchoolService] School updated successfully with schoolId: {}", schoolEntity.getId());

        ApiResponse response = new ApiResponse(
                mapInterface.toSchoolResponse(schoolEntity), "successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}