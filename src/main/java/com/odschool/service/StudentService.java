package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.StudentRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StudentEntity;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
import com.odschool.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentService {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    DivisionRepository divisionRepository;
    @Autowired
    MapInterface mapInterface;

    public ResponseEntity<Object> getAllStudents() {
        log.info("[SERVICE] Start fetching all students");
        List<StudentResponse> studentList = studentRepository.findAll().stream()
                .map(mapInterface::toStudentResponse)
                .collect(Collectors.toList());

        ApiResponse response = new ApiResponse(studentList, "Student list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> addStudent(StudentRequest studentRequest) {
        log.trace("[SERVICE] Start adding new Student: {}", studentRequest.getStudentName());
        try {
            StudentEntity findStudent = studentRepository.findByStudentNameAndDivisionEntity_Id(
                    studentRequest.getStudentName(), studentRequest.getDivisionId());

            if (findStudent == null) {
                DivisionEntity divisionEntity = divisionRepository.findById(studentRequest.getDivisionId()).orElse(null);
                if (divisionEntity == null) {
                    ApiResponse response = new ApiResponse(
                            "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
                    );
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }

                StudentEntity studentEntity = mapInterface.toStudentEntity(studentRequest);
                studentEntity.setDivisionEntity(divisionEntity);
                studentRepository.save(studentEntity);

                ApiResponse response = new ApiResponse(
                        mapInterface.toStudentResponse(studentEntity),
                        "new student added", true, HttpStatus.OK.value()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse response = new ApiResponse(
                        "STUDENT_EXIST_ERROR", "Student already exist", false, HttpStatus.CONFLICT.value()
                );
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while adding student", e);
            ApiResponse response = new ApiResponse(
                    "STUDENT_UNEXPECTED_ERROR", "Student unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> getStudentById(int studentId) {
        try {
            StudentEntity findStudent = studentRepository.findById(studentId).orElse(null);
            if (findStudent == null) {
                ApiResponse response = new ApiResponse(
                        "STUDENT_NOT_FOUND_ERROR", "Student not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            ApiResponse response = new ApiResponse(
                    mapInterface.toStudentResponse(findStudent), "student found successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while fetching student with studentId: {}", studentId, e);
            ApiResponse response = new ApiResponse(
                    "STUDENT_UNEXPECTED_ERROR", "Student unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> deleteStudent(int studentId) {
        try {
            StudentEntity studentEntity = studentRepository.findById(studentId).orElse(null);
            if (studentEntity == null) {
                ApiResponse response = new ApiResponse(
                        "STUDENT_NOT_FOUND_ERROR", "Student not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            studentRepository.deleteById(studentEntity.getId());
            ApiResponse response = new ApiResponse("Student Deleted", "successful", true, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while deleting student with studentId: {}", studentId, e);
            ApiResponse response = new ApiResponse(
                    "STUDENT_UNEXPECTED_ERROR", "Student unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity<Object> modifyStudent(StudentResponse studentDto, int studentId) {
        try {
            StudentEntity studentEntity = studentRepository.findById(studentId).orElse(null);
            if (studentEntity == null) {
                ApiResponse response = new ApiResponse(
                        "STUDENT_NOT_FOUND_ERROR", "Student not found", false, HttpStatus.NOT_FOUND.value()
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            studentEntity.setStudentName(studentDto.getStudentName());
            studentEntity.setObtainMarks(studentDto.getObtainMarks());
            studentEntity.setPercentage(studentDto.getPercentage());
            studentEntity.setResult(studentDto.getResult());
            studentRepository.save(studentEntity);

            ApiResponse response = new ApiResponse(
                    mapInterface.toStudentResponse(studentEntity), "successful", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[SERVICE] Unexpected error while modifying student with studentId: {}", studentId, e);
            ApiResponse response = new ApiResponse(
                    "STUDENT_UNEXPECTED_ERROR", "Student unexpected error", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}