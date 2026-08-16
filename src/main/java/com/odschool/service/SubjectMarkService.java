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

        List<SubjectMarkResponse> subjectMarkList = subjectMarkRepository.findAllByStudentEntity_Id(studentId).stream()
                .map(mapInterface::toSubjectMarkResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][SubjectMarkService] Fetched {} subject marks successfully for studentId: {}", subjectMarkList.size(), studentId);

        ApiResponse response = new ApiResponse(subjectMarkList, "Subject mark list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addSubjectMark(SubjectMarkRequest subjectMarkRequest) {
        log.info("[ODSCHOOL][SubjectMarkService] Start adding new SubjectMark: {} for studentId: {}",
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
        log.info("[ODSCHOOL][SubjectMarkService] Start modifying subject mark with subjectMarkId: {} and requested subject name: {}",
                subjectMarkId, subjectMarkDto.getSubjectName());

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
}