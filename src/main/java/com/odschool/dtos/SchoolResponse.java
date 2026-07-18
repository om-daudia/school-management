package com.odschool.dtos;

import lombok.Data;

@Data
public class SchoolResponse {
    public SchoolResponse(int id, String schoolName) {
        this.schoolName = schoolName;
        this.id = id;
    }

    int id;
    private String schoolName;
}
