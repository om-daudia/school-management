package com.odschool.dtos;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class ApiResponse {
     Object data;
     String message;
     boolean isSuccess;
     int httpStatus;

     public ApiResponse(Object data, String message, boolean isSuccess, int httpStatus) {
          this.data = data;
          this.message = message;
          this.isSuccess = isSuccess;
          this.httpStatus = httpStatus;
     }
}
