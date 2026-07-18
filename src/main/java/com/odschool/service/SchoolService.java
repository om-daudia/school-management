package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SchoolRequest;
import com.odschool.dtos.SchoolResponse;
import com.odschool.entity.SchoolEntity;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.SchoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        log.info("[SERVICE] Start fetching all schools");
        List<SchoolResponse> schoolList = schoolRepository.findAll().stream()
                .map(mapInterface::toSchoolResponse)
                .collect(Collectors.toList());

        log.info("[SERVICE] Fetched {} schools", schoolList.size());
        ApiResponse response = new ApiResponse(schoolList, "School list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public ResponseEntity<Object> addSchool(SchoolRequest schoolRequest) {
        log.trace("[SERVICE] Start adding new School with school name: {}", schoolRequest.getSchoolName());
        try {
            SchoolEntity findSchool = schoolRepository.findBySchoolName(schoolRequest.getSchoolName());
            if (findSchool == null) {
                SchoolEntity schoolEntity = new SchoolEntity(schoolRequest.getSchoolName());
                schoolRepository.save(schoolEntity);

                log.info("[SERVICE] school add successful and sending successful responseEntity");
                log.trace("[SERVICE] exit from addSchool method");

                ApiResponse response = new ApiResponse(
                        mapInterface.toSchoolResponse(schoolEntity),
                        "new school added",
                        true,
                        HttpStatus.OK.value()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                log.info("[SERVICE] school already exist and sending unsuccessful responseEntity");
                log.trace("[SERVICE] Finished adding school");
                ApiResponse response = new ApiResponse(
                        "SCHOOL_EXIST_ERROR", "School already exist", false, HttpStatus.CONFLICT.value()
                );
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while adding school", e);
            ApiResponse response = new ApiResponse(
                    "SCHOOL_UNEXPECTED_ERROR", "School unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getSchoolById(int schoolId) {
        log.trace("[SERVICE] Start fetching school with schoolId: {}", schoolId);
        try {
            SchoolEntity findSchool = schoolRepository.findById(schoolId).orElse(null);

            if (findSchool == null) {
                log.warn("[SERVICE] school not found with schoolId: {}", schoolId);
                ApiResponse response = new ApiResponse(
                        "SCHOOL_NOT_FOUND_ERROR", "School not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            SchoolResponse schoolResponse = mapInterface.toSchoolResponse(findSchool);
            log.info("[SERVICE] school found with schoolId {} and returning SchoolResponse", schoolId);

            ApiResponse response = new ApiResponse(schoolResponse, "school found successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while fetching school with schoolId: {}", schoolId, e);
            ApiResponse response = new ApiResponse(
                    "SCHOOL_UNEXPECTED_ERROR", "School unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }


    public ResponseEntity<Object> deleteSchool(int schoolId) {
        log.trace("[SERVICE] Start deleteSchool method with schoolId {}", schoolId);
        try {
            SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);

            if (schoolEntity == null) {
                log.warn("[SERVICE] school not found with schoolId: {}", schoolId);
                ApiResponse response = new ApiResponse(
                        "SCHOOL_NOT_FOUND_ERROR", "School not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            log.debug("[SERVICE] schoolEntity schoolId {}", schoolEntity.getId());
            schoolRepository.deleteById(schoolEntity.getId());
            log.info("[SERVICE] school deleted successful with schoolId {}", schoolEntity.getId());

            ApiResponse response = new ApiResponse("School Deleted", "successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while deleting school with schoolId: {}", schoolId, e);
            ApiResponse response = new ApiResponse(
                    "SCHOOL_UNEXPECTED_ERROR", "School unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> modifySchool(SchoolResponse schoolDto, int schoolId) {
        log.trace("[SERVICE] Start modifySchool method with schoolDTO: {} and schoolId: {}", schoolDto, schoolId);
        try {
            SchoolEntity schoolEntity = schoolRepository.findById(schoolId).orElse(null);

            if (schoolEntity == null) {
                log.warn("[SERVICE] school not found with schoolId: {}", schoolId);
                ApiResponse response = new ApiResponse(
                        "SCHOOL_NOT_FOUND_ERROR", "School not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            log.debug("[SERVICE] old schoolName: {}", schoolEntity.getSchoolName());
            schoolEntity.setSchoolName(schoolDto.getSchoolName());
            log.debug("[SERVICE] updated schoolName: {}", schoolEntity.getSchoolName());
            schoolRepository.save(schoolEntity);
            log.info("[SERVICE] update successful with schoolId: {}", schoolEntity.getId());

            ApiResponse response = new ApiResponse(
                    mapInterface.toSchoolResponse(schoolEntity), "successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while modifying school with schoolId: {}", schoolId, e);
            ApiResponse response = new ApiResponse(
                    "SCHOOL_UNEXPECTED_ERROR", "School unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
