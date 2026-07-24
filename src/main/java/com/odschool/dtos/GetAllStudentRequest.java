package com.odschool.dtos;

import lombok.Data;

@Data
public class GetAllStudentRequest {
    int schoolId;
    int divisionId;
    int standardId;
}
