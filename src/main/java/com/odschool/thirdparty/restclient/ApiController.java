package com.odschool.thirdparty.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/restclient")
public class ApiController {

    @Autowired
    ApiService apiService;
    @GetMapping
    public String getMethod() {
        return apiService.getAllUser();
    }

    @PostMapping
    public String postMethod() {
        return apiService.addUser();
    }

    @DeleteMapping("/{userId}")
    public String deleteMethod(@PathVariable int userId) {
        return apiService.deleteUser();
    }

}
