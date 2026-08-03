package com.odschool.thirdparty.restclient;

import com.odschool.dtos.SchoolRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiService {
    @Autowired
    RestClient restClient;

    String url = "http://localhost:9090/api/thirdparty";
    public String getAllUser(){
        return restClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("server error");
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new RuntimeException("unexpected error");
                }))
                .body(String.class);
    }

    public String addUser(){
        SchoolRequest schoolRequest = new SchoolRequest("jumbo school");
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(schoolRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("server error");
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new RuntimeException("unexpected error");
                }))
                .body(String.class);
    }

    public String deleteUser(){
        SchoolRequest schoolRequest = new SchoolRequest("jumbo school");
        return restClient.delete()
                .uri(url+"/{userId}", 12)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("server error");
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new RuntimeException("unexpected error");
                }))
                .body(String.class);
    }
}
