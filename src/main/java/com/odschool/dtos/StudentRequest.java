package com.odschool.dtos;

import lombok.Data;

@Data
public class StudentRequest {
    private String studentName;
    private float obtainMarks;
    private float percentage;
    private String result;
}
