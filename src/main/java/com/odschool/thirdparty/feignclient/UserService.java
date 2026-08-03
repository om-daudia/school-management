package com.odschool.thirdparty.feignclient;

import com.odschool.dtos.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserClient userClient;

    public String getAllUsers(){
        return userClient.getALllUsers();
    }

    public String saveUser(){
        return userClient.saveUser();
    }
}
