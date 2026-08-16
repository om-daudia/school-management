package com.odschool.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String httpStatus;
    private int httpCode;
}
