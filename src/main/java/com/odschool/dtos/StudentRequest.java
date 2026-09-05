package com.odschool.dtos;

import lombok.Data;

@Data
public class StudentRequest {
    private String studentName;
    private double obtainMarks;
    private double percentage;
    private String result;
}
