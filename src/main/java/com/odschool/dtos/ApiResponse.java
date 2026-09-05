package com.odschool.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
     Object data;
     String message;
     boolean isSuccess;
     int httpStatus;
}
