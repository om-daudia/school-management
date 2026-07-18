package com.odschool.dtos;

import lombok.Data;

@Data
public class StandardRequest {
    private int id;
    private int standard;
    private int schoolId;
}