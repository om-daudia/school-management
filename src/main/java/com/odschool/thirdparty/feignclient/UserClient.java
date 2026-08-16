package com.odschool.thirdparty.feignclient;

import com.odschool.dtos.StudentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "user-service", url = "http://localhost:9090/api/thirdparty")
public interface UserClient {
    @GetMapping()
    String getALllUsers();

    @PostMapping()
    String saveUser();
}
