package com.odschool.controller;

import com.odschool.dtos.GetAllStudentRequest;
import com.odschool.dtos.StudentRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("odschool/student")
public class StudentController {
    @Autowired
    StudentService studentService;

    @GetMapping()
    public ResponseEntity<Object> getAllStudents(@PathVariable int divisionId) {
        return studentService.getAllStudents();
    }
    @PostMapping("/by-school")
    public ResponseEntity<Object> getAllStudentsOfSchool(@RequestBody GetAllStudentRequest request) {
        return studentService.getAllStudentsOfSchool(request);
    }

    @PostMapping("/by-standard")
    public ResponseEntity<Object> getAllStudentsOfStandard(@RequestBody GetAllStudentRequest request) {
        return studentService.getAllStudentsOfStandard(request);
    }

    @PostMapping("/by-division")
    public ResponseEntity<Object> getAllStudentsOfDivision(@RequestBody GetAllStudentRequest request) {
        return studentService.getAllStudentsOfDivision(request);
    }
    @PostMapping()
    public ResponseEntity<Object> addStudent(@RequestBody StudentRequest studentRequest, @PathVariable int divisionId) {
        return studentService.addStudent(studentRequest, divisionId);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Object> getStudentById(@PathVariable int studentId) {
        return studentService.getStudentById(studentId);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Object> deleteStudent(@PathVariable int studentId) {
        return studentService.deleteStudent(studentId);
    }

    @PatchMapping("/{studentId}")
    public ResponseEntity<Object> modifyStudent(@RequestBody StudentResponse studentResponseDto, @PathVariable int studentId) {
        return studentService.modifyStudent(studentResponseDto, studentId);
    }
}