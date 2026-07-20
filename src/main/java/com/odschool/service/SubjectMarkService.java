package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SubjectMarkRequest;
import com.odschool.dtos.SubjectMarkResponse;
import com.odschool.entity.StudentEntity;
import com.odschool.entity.SubjectMarkEntity;
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

    public ResponseEntity<Object> getAllSubjectMarks() {
        log.info("[SERVICE] Start fetching all subject marks");
        List<SubjectMarkResponse> subjectMarkList = subjectMarkRepository.findAll().stream()
                .map(mapInterface::toSubjectMarkResponse)
                .collect(Collectors.toList());

        ApiResponse response = new ApiResponse(subjectMarkList, "Subject mark list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addSubjectMark(SubjectMarkRequest subjectMarkRequest) {
        log.trace("[SERVICE] Start adding new SubjectMark: {}", subjectMarkRequest.getSubjectName());
        try {
            SubjectMarkEntity findSubjectMark = subjectMarkRepository.findBySubjectNameAndStudentEntity_Id(
                    subjectMarkRequest.getSubjectName(), subjectMarkRequest.getStudentId());

            if (findSubjectMark == null) {
                StudentEntity studentEntity = studentRepository.findById(subjectMarkRequest.getStudentId()).orElse(null);
                if (studentEntity == null) {
                    log.warn("[SERVICE] student not found with studentId: {}", subjectMarkRequest.getStudentId());
                    ApiResponse response = new ApiResponse(
                            "STUDENT_NOT_FOUND_ERROR", "Student not found", false, HttpStatus.NOT_FOUND.value()
                    );
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }

                SubjectMarkEntity subjectMarkEntity = mapInterface.toSubjectMarkEntity(subjectMarkRequest);
                subjectMarkEntity.setStudentEntity(studentEntity);
                subjectMarkRepository.save(subjectMarkEntity);

                log.info("[SERVICE] subject mark add successful");
                ApiResponse response = new ApiResponse(
                        mapInterface.toSubjectMarkResponse(subjectMarkEntity),
                        "new subject mark added", true, HttpStatus.OK.value()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                log.info("[SERVICE] subject mark already exists for this student");
                ApiResponse response = new ApiResponse(
                        "SUBJECT_MARK_EXIST_ERROR", "Subject mark already exist", false, HttpStatus.CONFLICT.value()
                );
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while adding subject mark", e);
            ApiResponse response = new ApiResponse(
                    "SUBJECT_MARK_UNEXPECTED_ERROR", "Subject mark unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getSubjectMarkById(int subjectMarkId) {
        log.trace("[SERVICE] Start fetching subject mark with subjectMarkId: {}", subjectMarkId);
        try {
            SubjectMarkEntity findSubjectMark = subjectMarkRepository.findById(subjectMarkId).orElse(null);
            if (findSubjectMark == null) {
                log.warn("[SERVICE] subject mark not found with subjectMarkId: {}", subjectMarkId);
                ApiResponse response = new ApiResponse(
                        "SUBJECT_MARK_NOT_FOUND_ERROR", "Subject mark not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            ApiResponse response = new ApiResponse(
                    mapInterface.toSubjectMarkResponse(findSubjectMark), "subject mark found successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while fetching subject mark with subjectMarkId: {}", subjectMarkId, e);
            ApiResponse response = new ApiResponse(
                    "SUBJECT_MARK_UNEXPECTED_ERROR", "Subject mark unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> deleteSubjectMark(int subjectMarkId) {
        log.trace("[SERVICE] Start deleteSubjectMark method with subjectMarkId {}", subjectMarkId);
        try {
            SubjectMarkEntity subjectMarkEntity = subjectMarkRepository.findById(subjectMarkId).orElse(null);
            if (subjectMarkEntity == null) {
                log.warn("[SERVICE] subject mark not found with subjectMarkId: {}", subjectMarkId);
                ApiResponse response = new ApiResponse(
                        "SUBJECT_MARK_NOT_FOUND_ERROR", "Subject mark not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            subjectMarkRepository.deleteById(subjectMarkEntity.getId());
            log.info("[SERVICE] subject mark deleted successful with subjectMarkId {}", subjectMarkEntity.getId());

            ApiResponse response = new ApiResponse("Subject Mark Deleted", "successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while deleting subject mark with subjectMarkId: {}", subjectMarkId, e);
            ApiResponse response = new ApiResponse(
                    "SUBJECT_MARK_UNEXPECTED_ERROR", "Subject mark unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> modifySubjectMark(SubjectMarkResponse subjectMarkDto, int subjectMarkId) {
        log.trace("[SERVICE] Start modifySubjectMark with subjectMarkDTO: {} and subjectMarkId: {}", subjectMarkDto, subjectMarkId);
        try {
            SubjectMarkEntity subjectMarkEntity = subjectMarkRepository.findById(subjectMarkId).orElse(null);
            if (subjectMarkEntity == null) {
                log.warn("[SERVICE] subject mark not found with subjectMarkId: {}", subjectMarkId);
                ApiResponse response = new ApiResponse(
                        "SUBJECT_MARK_NOT_FOUND_ERROR", "Subject mark not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            subjectMarkEntity.setSubjectName(subjectMarkDto.getSubjectName());
            subjectMarkEntity.setMarks(subjectMarkDto.getMarks());
            subjectMarkRepository.save(subjectMarkEntity);
            log.info("[SERVICE] update successful with subjectMarkId: {}", subjectMarkEntity.getId());

            ApiResponse response = new ApiResponse(
                    mapInterface.toSubjectMarkResponse(subjectMarkEntity), "successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while modifying subject mark with subjectMarkId: {}", subjectMarkId, e);
            ApiResponse response = new ApiResponse(
                    "SUBJECT_MARK_UNEXPECTED_ERROR", "Subject mark unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}