package com.odschool.controller;

import com.odschool.dtos.StandardRequest;
import com.odschool.dtos.StandardResponse;
import com.odschool.service.StandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/odschool/school/{schoolId}/standard")
public class StandardController {
    @Autowired
    StandardService standardService;

    @GetMapping()
    public ResponseEntity<Object> getAllStandards() {
        return standardService.getAllStandards();
    }

    @PostMapping()
    public ResponseEntity<Object> addStandard(@RequestBody StandardRequest standardRequest) {
        return standardService.addStandard(standardRequest);
    }

    @GetMapping("/{standardId}")
    public ResponseEntity<Object> getStandardById(@PathVariable int standardId) {
        return standardService.getStandardById(standardId);
    }

    @DeleteMapping("/{standardId}")
    public ResponseEntity<Object> deleteStandard(@PathVariable int standardId) {
        return standardService.deleteStandard(standardId);
    }

    @PatchMapping("/{standardId}")
    public ResponseEntity<Object> modifyStandard(@RequestBody StandardResponse standardResponseDto, @PathVariable int standardId) {
        return standardService.modifyStandard(standardResponseDto, standardId);
    }
}