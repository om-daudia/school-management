package com.odschool.dtos;

import lombok.Data;

@Data
public class StudentResponse {
    private int id;
    private String studentName;
    private float obtainMarks;
    private float percentage;
    private String result;
    private int divisionId;
}