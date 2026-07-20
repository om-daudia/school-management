package com.odschool.controller;

import com.odschool.dtos.SubjectMarkRequest;
import com.odschool.dtos.SubjectMarkResponse;
import com.odschool.service.SubjectMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/{studentId}/subjectmark")
public class SubjectMarkController {
    @Autowired
    SubjectMarkService subjectMarkService;

    @GetMapping()
    public ResponseEntity<Object> getAllSubjectMarks() {
        return subjectMarkService.getAllSubjectMarks();
    }

    @PostMapping()
    public ResponseEntity<Object> addSubjectMark(@RequestBody SubjectMarkRequest subjectMarkRequest) {
        return subjectMarkService.addSubjectMark(subjectMarkRequest);
    }

    @GetMapping("/{subjectMarkId}")
    public ResponseEntity<Object> getSubjectMarkById(@PathVariable int subjectMarkId) {
        return subjectMarkService.getSubjectMarkById(subjectMarkId);
    }

    @DeleteMapping("/{subjectMarkId}")
    public ResponseEntity<Object> deleteSubjectMark(@PathVariable int subjectMarkId) {
        return subjectMarkService.deleteSubjectMark(subjectMarkId);
    }

    @PatchMapping("/{subjectMarkId}")
    public ResponseEntity<Object> modifySubjectMark(@RequestBody SubjectMarkResponse subjectMarkResponseDto, @PathVariable int subjectMarkId) {
        return subjectMarkService.modifySubjectMark(subjectMarkResponseDto, subjectMarkId);
    }
}