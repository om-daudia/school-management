package com.odschool.dtos;

import lombok.Data;

@Data
public class SubjectMarkResponse {
    private int id;
    private String subjectName;
    private int marks;
    private int studentId;
}