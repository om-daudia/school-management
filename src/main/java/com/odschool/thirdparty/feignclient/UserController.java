package com.odschool.thirdparty.feignclient;


import com.odschool.dtos.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feignclient")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping()
    public String getAll(){
        return userService.getAllUsers();
    }
    @PostMapping()
    public String saveUser(){
        return userService.saveUser();
    }
}
