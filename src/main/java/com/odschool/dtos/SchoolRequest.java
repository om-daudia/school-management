package com.odschool.dtos;

import lombok.Data;

@Data
public class SchoolRequest {
    String schoolName;

    public SchoolRequest(String schoolName) {
        this.schoolName = schoolName;
    }
}
