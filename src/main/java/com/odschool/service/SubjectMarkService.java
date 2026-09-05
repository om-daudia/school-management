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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectMarkService {
    @Autowired
    SubjectMarkRepository subjectMarkRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    MapInterface mapInterface;

    public ResponseEntity<Object> getAllSubjectMarks(int studentId) {
        log.info("[ODSCHOOL][SubjectMarkService] Start fetching all subject marks for studentId: {}", studentId);

        validateStudentId(studentId);

        List<SubjectMarkResponse> subjectMarkList = subjectMarkRepository.findAllByStudentEntity_Id(studentId).stream()
                .map(mapInterface::toSubjectMarkResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][SubjectMarkService] Fetched {} subject marks successfully for studentId: {}", subjectMarkList.size(), studentId);

        ApiResponse response = new ApiResponse(subjectMarkList, "Subject mark list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addSubjectMark(SubjectMarkRequest subjectMarkRequest) {
        log.info("[ODSCHOOL][SubjectMarkService] Start adding new SubjectMark for studentId: {}",
                subjectMarkRequest == null ? null : subjectMarkRequest.getStudentId());

        validateSubjectMarkRequest(subjectMarkRequest);

        log.info("[ODSCHOOL][SubjectMarkService] Validated request, subject name: {} for studentId: {}",
                subjectMarkRequest.getSubjectName(), subjectMarkRequest.getStudentId());

        SubjectMarkEntity findSubjectMark = subjectMarkRepository.findBySubjectNameAndStudentEntity_Id(
                subjectMarkRequest.getSubjectName(), subjectMarkRequest.getStudentId());

        if (findSubjectMark == null) {
            StudentEntity studentEntity = studentRepository.findById(subjectMarkRequest.getStudentId()).orElse(null);
            if (studentEntity == null) {
                log.error("[ODSCHOOL][SubjectMarkService] Error while adding subject mark - Student not found with id={}",
                        subjectMarkRequest.getStudentId());
                throw new ApiException("Student not found with id=" + subjectMarkRequest.getStudentId(), HttpStatus.NOT_FOUND);
            }

            SubjectMarkEntity subjectMarkEntity = mapInterface.toSubjectMarkEntity(subjectMarkRequest);
            subjectMarkEntity.setStudentEntity(studentEntity);
            subjectMarkRepository.save(subjectMarkEntity);

            log.info("[ODSCHOOL][SubjectMarkService] Subject mark added successfully with subjectMarkId: {} and subject name: {} for studentId: {}",
                    subjectMarkEntity.getId(), subjectMarkEntity.getSubjectName(), subjectMarkRequest.getStudentId());

            ApiResponse response = new ApiResponse(
                    mapInterface.toSubjectMarkResponse(subjectMarkEntity),
                    "new subject mark added", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            log.error("[ODSCHOOL][SubjectMarkService] Error while adding subject mark - subject name: {} already exists for studentId: {}",
                    subjectMarkRequest.getSubjectName(), subjectMarkRequest.getStudentId());
            throw new ApiException("Subject mark already exist", HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getSubjectMarkById(int subjectMarkId) {
        log.info("[ODSCHOOL][SubjectMarkService] Start fetching subject mark with subjectMarkId: {}", subjectMarkId);

        validateSubjectMarkId(subjectMarkId);

        SubjectMarkEntity findSubjectMark = subjectMarkRepository.findById(subjectMarkId).orElse(null);
        if (findSubjectMark == null) {
            log.error("[ODSCHOOL][SubjectMarkService] Error while fetching subject mark - Subject Mark not found with id={}", subjectMarkId);
            throw new ApiException("Subject Mark not found with id=" + subjectMarkId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][SubjectMarkService] Subject mark found successfully with subjectMarkId: {}", subjectMarkId);

        ApiResponse response = new ApiResponse(
                mapInterface.toSubjectMarkResponse(findSubjectMark), "subject mark found successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> deleteSubjectMark(int subjectMarkId) {
        log.info("[ODSCHOOL][SubjectMarkService] Start deleting subject mark with subjectMarkId: {}", subjectMarkId);

        validateSubjectMarkId(subjectMarkId);

        SubjectMarkEntity subjectMarkEntity = subjectMarkRepository.findById(subjectMarkId).orElse(null);
        if (subjectMarkEntity == null) {
            log.error("[ODSCHOOL][SubjectMarkService] Error while deleting subject mark - Subject Mark not found with id={}", subjectMarkId);
            throw new ApiException("Subject Mark not found with id=" + subjectMarkId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][SubjectMarkService] Subject mark found with subjectMarkId: {}, proceeding to delete", subjectMarkEntity.getId());

        subjectMarkRepository.deleteById(subjectMarkEntity.getId());

        log.info("[ODSCHOOL][SubjectMarkService] Subject mark deleted successfully with subjectMarkId: {}", subjectMarkEntity.getId());

        ApiResponse response = new ApiResponse("Subject Mark Deleted", "successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> modifySubjectMark(SubjectMarkResponse subjectMarkDto, int subjectMarkId) {
        log.info("[ODSCHOOL][SubjectMarkService] Start modifying subject mark with subjectMarkId: {}", subjectMarkId);

        validateSubjectMarkId(subjectMarkId);
        validateSubjectMarkDto(subjectMarkDto);

        log.info("[ODSCHOOL][SubjectMarkService] Validated request, requested subject name: {}", subjectMarkDto.getSubjectName());

        SubjectMarkEntity subjectMarkEntity = subjectMarkRepository.findById(subjectMarkId).orElse(null);
        if (subjectMarkEntity == null) {
            log.error("[ODSCHOOL][SubjectMarkService] Error while modifying subject mark - Subject Mark not found with id={}", subjectMarkId);
            throw new ApiException("Subject Mark not found with id=" + subjectMarkId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][SubjectMarkService] Old subject mark data for subjectMarkId: {} - subjectName: {}, marks: {}",
                subjectMarkId, subjectMarkEntity.getSubjectName(), subjectMarkEntity.getMarks());

        subjectMarkEntity.setSubjectName(subjectMarkDto.getSubjectName());
        subjectMarkEntity.setMarks(subjectMarkDto.getMarks());
        subjectMarkRepository.save(subjectMarkEntity);

        log.info("[ODSCHOOL][SubjectMarkService] Subject mark updated successfully with subjectMarkId: {} - subjectName: {}, marks: {}",
                subjectMarkEntity.getId(), subjectMarkEntity.getSubjectName(), subjectMarkEntity.getMarks());

        ApiResponse response = new ApiResponse(
                mapInterface.toSubjectMarkResponse(subjectMarkEntity), "successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateStudentId(int studentId) {
        if (studentId <= 0) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: studentId {} is not a positive integer", studentId);
            throw new ApiException("Student id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSubjectMarkId(int subjectMarkId) {
        if (subjectMarkId <= 0) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: subjectMarkId {} is not a positive integer", subjectMarkId);
            throw new ApiException("Subject mark id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSubjectMarkRequest(SubjectMarkRequest subjectMarkRequest) {
        if (subjectMarkRequest == null) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: subjectMarkRequest is null");
            throw new ApiException("Subject mark request must not be null", HttpStatus.BAD_REQUEST);
        }
        validateStudentId(subjectMarkRequest.getStudentId());
        validateSubjectMarkFields(subjectMarkRequest.getSubjectName(), subjectMarkRequest.getMarks());
    }

    private void validateSubjectMarkDto(SubjectMarkResponse subjectMarkDto) {
        if (subjectMarkDto == null) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: subjectMarkDto is null");
            throw new ApiException("Subject mark request must not be null", HttpStatus.BAD_REQUEST);
        }
        validateSubjectMarkFields(subjectMarkDto.getSubjectName(), subjectMarkDto.getMarks());
    }

    private void validateSubjectMarkFields(String subjectName, double marks) {
        if (subjectName == null || subjectName.isBlank()) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: subjectName is blank");
            throw new ApiException("Subject name must not be blank", HttpStatus.BAD_REQUEST);
        }
        if (marks < 0) {
            log.info("[ODSCHOOL][SubjectMarkService] Validation failed: marks {} is negative", marks);
            throw new ApiException("Marks must not be negative", HttpStatus.BAD_REQUEST);
        }
    }
}