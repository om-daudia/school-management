package com.odschool.dtos;

import lombok.Data;

@Data
public class SubjectMarkRequest {
    private String subjectName;
    private int marks;
    private int studentId;
}