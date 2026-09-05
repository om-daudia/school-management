package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.GetAllStudentRequest;
import com.odschool.dtos.StudentRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StudentEntity;
import com.odschool.exception.ApiException;
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
        log.info("[ODSCHOOL][StudentService] Start fetching all students");

        List<StudentResponse> studentList = studentRepository.findAll().stream()
                .map(mapInterface::toStudentResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][StudentService] Fetched {} students successfully", studentList.size());

        ApiResponse response = new ApiResponse(studentList, "Student All list", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> getAllStudentsOfSchool(GetAllStudentRequest request) {
        log.info("[ODSCHOOL][StudentService] Start fetching all students for schoolId: {}",
                request == null ? null : request.getSchoolId());

        validateGetAllStudentRequest(request);
        validateSchoolId(request.getSchoolId());

        List<StudentResponse> studentList = studentRepository.findAllByDivisionEntity_StandardEntity_SchoolEntity_Id(request.getSchoolId()).stream()
                .map(mapInterface::toStudentResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][StudentService] Fetched {} students successfully for schoolId: {}", studentList.size(), request.getSchoolId());

        ApiResponse response = new ApiResponse(studentList, "Student All List of SchoolId: " + request.getSchoolId(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> getAllStudentsOfDivision(GetAllStudentRequest request) {
        log.info("[ODSCHOOL][StudentService] Start fetching all students for divisionId: {}",
                request == null ? null : request.getDivisionId());

        validateGetAllStudentRequest(request);
        validateDivisionId(request.getDivisionId());

        List<StudentResponse> studentList = studentRepository.findAllByDivisionEntityId(request.getDivisionId()).stream()
                .map(mapInterface::toStudentResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][StudentService] Fetched {} students successfully for divisionId: {}", studentList.size(), request.getDivisionId());

        ApiResponse response = new ApiResponse(studentList, "Student All List of DivisionId: " + request.getDivisionId(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> getAllStudentsOfStandard(GetAllStudentRequest request) {
        log.info("[ODSCHOOL][StudentService] Start fetching all students for standardId: {}",
                request == null ? null : request.getStandardId());

        validateGetAllStudentRequest(request);
        validateStandardId(request.getStandardId());

        List<StudentResponse> studentList = studentRepository.findAllByDivisionEntity_StandardEntity_Id(request.getStandardId()).stream()
                .map(mapInterface::toStudentResponse)
                .collect(Collectors.toList());

        log.info("[ODSCHOOL][StudentService] Fetched {} students successfully for standardId: {}", studentList.size(), request.getStandardId());

        ApiResponse response = new ApiResponse(studentList, "Student All list of StandardId: " + request.getStandardId(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public ResponseEntity<Object> addStudent(StudentRequest studentRequest, int divisionId) {
        log.info("[ODSCHOOL][StudentService] Start adding new Student for divisionId: {}", divisionId);

        validateDivisionId(divisionId);
        validateStudentRequest(studentRequest);

        log.info("[ODSCHOOL][StudentService] Validated request, student name: {} for divisionId: {}",
                studentRequest.getStudentName(), divisionId);

        StudentEntity findStudent = studentRepository.findByStudentNameAndDivisionEntity_Id(
                studentRequest.getStudentName(), divisionId);

        if (findStudent == null) {
            DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
            if (divisionEntity == null) {
                log.error("[ODSCHOOL][StudentService] Error while adding student - Division not found with id={}", divisionId);
                throw new ApiException("Division not found with id=" + divisionId, HttpStatus.NOT_FOUND);
            }

            StudentEntity studentEntity = mapInterface.toStudentEntity(studentRequest);
            studentEntity.setDivisionEntity(divisionEntity);
            studentRepository.save(studentEntity);

            log.info("[ODSCHOOL][StudentService] Student added successfully with studentId: {} and student name: {} for divisionId: {}",
                    studentEntity.getId(), studentEntity.getStudentName(), divisionId);

            ApiResponse response = new ApiResponse(
                    mapInterface.toStudentResponse(studentEntity),
                    "new student added", true, HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            log.error("[ODSCHOOL][StudentService] Error while adding student - student name: {} already exists for divisionId: {}",
                    studentRequest.getStudentName(), divisionId);
            throw new ApiException("Student already exist", HttpStatus.CONFLICT);
        }

    }

    public ResponseEntity<Object> getStudentById(int studentId) {
        log.info("[ODSCHOOL][StudentService] Start fetching student with studentId: {}", studentId);

        validateStudentId(studentId);

        StudentEntity findStudent = studentRepository.findById(studentId).orElse(null);
        if (findStudent == null) {
            log.error("[ODSCHOOL][StudentService] Error while fetching student - Student not found with id={}", studentId);
            throw new ApiException("Student not found with id=" + studentId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][StudentService] Student found successfully with studentId: {}", studentId);

        ApiResponse response = new ApiResponse(
                mapInterface.toStudentResponse(findStudent), "student found successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> deleteStudent(int studentId) {
        log.info("[ODSCHOOL][StudentService] Start deleting student with studentId: {}", studentId);

        validateStudentId(studentId);

        StudentEntity studentEntity = studentRepository.findById(studentId).orElse(null);
        if (studentEntity == null) {
            log.error("[ODSCHOOL][StudentService] Error while deleting student - Student not found with id={}", studentId);
            throw new ApiException("Student not found with id=" + studentId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][StudentService] Student found with studentId: {}, proceeding to delete", studentEntity.getId());

        studentRepository.deleteById(studentEntity.getId());

        log.info("[ODSCHOOL][StudentService] Student deleted successfully with studentId: {}", studentEntity.getId());

        ApiResponse response = new ApiResponse("Student Deleted", "successful", true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    public ResponseEntity<Object> modifyStudent(StudentResponse studentDto, int studentId) {
        log.info("[ODSCHOOL][StudentService] Start modifying student with studentId: {}", studentId);

        validateStudentId(studentId);
        validateStudentDto(studentDto);

        log.info("[ODSCHOOL][StudentService] Validated request, requested student name: {}", studentDto.getStudentName());

        StudentEntity studentEntity = studentRepository.findById(studentId).orElse(null);
        if (studentEntity == null) {
            log.error("[ODSCHOOL][StudentService] Error while modifying student - Student not found with id={}", studentId);
            throw new ApiException("Student not found with id=" + studentId, HttpStatus.NOT_FOUND);
        }

        log.info("[ODSCHOOL][StudentService] Old student data for studentId: {} - name: {}, obtainMarks: {}, percentage: {}, result: {}",
                studentId, studentEntity.getStudentName(), studentEntity.getObtainMarks(),
                studentEntity.getPercentage(), studentEntity.getResult());

        studentEntity.setStudentName(studentDto.getStudentName());
        studentEntity.setObtainMarks(studentDto.getObtainMarks());
        studentEntity.setPercentage(studentDto.getPercentage());
        studentEntity.setResult(studentDto.getResult());
        studentRepository.save(studentEntity);

        log.info("[ODSCHOOL][StudentService] Student updated successfully with studentId: {} - name: {}, obtainMarks: {}, percentage: {}, result: {}",
                studentEntity.getId(), studentEntity.getStudentName(), studentEntity.getObtainMarks(),
                studentEntity.getPercentage(), studentEntity.getResult());

        ApiResponse response = new ApiResponse(
                mapInterface.toStudentResponse(studentEntity), "successful", true, HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    private void validateStudentId(int studentId) {
        if (studentId <= 0) {
            log.info("[ODSCHOOL][StudentService] Validation failed: studentId {} is not a positive integer", studentId);
            throw new ApiException("Student id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDivisionId(int divisionId) {
        if (divisionId <= 0) {
            log.info("[ODSCHOOL][StudentService] Validation failed: divisionId {} is not a positive integer", divisionId);
            throw new ApiException("Division id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSchoolId(int schoolId) {
        if (schoolId <= 0) {
            log.info("[ODSCHOOL][StudentService] Validation failed: schoolId {} is not a positive integer", schoolId);
            throw new ApiException("School id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateStandardId(int standardId) {
        if (standardId <= 0) {
            log.info("[ODSCHOOL][StudentService] Validation failed: standardId {} is not a positive integer", standardId);
            throw new ApiException("Standard id must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateGetAllStudentRequest(GetAllStudentRequest request) {
        if (request == null) {
            log.info("[ODSCHOOL][StudentService] Validation failed: GetAllStudentRequest is null");
            throw new ApiException("Get all student request must not be null", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateStudentRequest(StudentRequest studentRequest) {
        if (studentRequest == null) {
            log.info("[ODSCHOOL][StudentService] Validation failed: studentRequest is null");
            throw new ApiException("Student request must not be null", HttpStatus.BAD_REQUEST);
        }
        validateStudentFields(studentRequest.getStudentName(), studentRequest.getObtainMarks(),
                studentRequest.getPercentage(), studentRequest.getResult());
    }

    private void validateStudentDto(StudentResponse studentDto) {
        if (studentDto == null) {
            log.info("[ODSCHOOL][StudentService] Validation failed: studentDto is null");
            throw new ApiException("Student request must not be null", HttpStatus.BAD_REQUEST);
        }
        validateStudentFields(studentDto.getStudentName(), studentDto.getObtainMarks(),
                studentDto.getPercentage(), studentDto.getResult());
    }

    private void validateStudentFields(String studentName, double obtainMarks, double percentage, String result) {
        if (studentName == null || studentName.isBlank()) {
            log.info("[ODSCHOOL][StudentService] Validation failed: studentName is blank");
            throw new ApiException("Student name must not be blank", HttpStatus.BAD_REQUEST);
        }
        if (obtainMarks < 0) {
            log.info("[ODSCHOOL][StudentService] Validation failed: obtainMarks {} is negative", obtainMarks);
            throw new ApiException("Obtained marks must not be negative", HttpStatus.BAD_REQUEST);
        }
        if (percentage < 0 || percentage > 100) {
            log.info("[ODSCHOOL][StudentService] Validation failed: percentage {} is out of range", percentage);
            throw new ApiException("Percentage must be between 0 and 100", HttpStatus.BAD_REQUEST);
        }
        if (result == null || result.isBlank()) {
            log.info("[ODSCHOOL][StudentService] Validation failed: result is blank");
            throw new ApiException("Result must not be blank", HttpStatus.BAD_REQUEST);
        }
    }
}