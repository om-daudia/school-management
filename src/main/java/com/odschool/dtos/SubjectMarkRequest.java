package com.odschool.dtos;

import lombok.Data;

@Data
public class SubjectMarkRequest {
    private String subjectName;
    private double marks;
    private int studentId;
}