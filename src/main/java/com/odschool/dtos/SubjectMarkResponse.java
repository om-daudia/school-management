package com.odschool.dtos;

import lombok.Data;

@Data
public class SubjectMarkResponse {
    private int id;
    private String subjectName;
    private double marks;
    private int studentId;
}