package com.odschool.controller;

import com.odschool.dtos.StandardRequest;
import com.odschool.service.StandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.odschool.util.TraceIdUtil.setTraceId;

@RestController
@RequestMapping("odschool/school/{schoolId}/standard")
public class StandardController {
    @Autowired
    StandardService standardService;

    @GetMapping()
    public ResponseEntity<Object> getAllStandards(@PathVariable int schoolId) {
        return standardService.getAllStandards(schoolId);
    }

    @PostMapping()
    public ResponseEntity<Object> addStandard(@RequestBody StandardRequest standardRequest, @PathVariable int schoolId) {
        return standardService.addStandard(standardRequest, schoolId);
    }

    @GetMapping("/{standardId}")
    public ResponseEntity<Object> getStandardById(@PathVariable int standardId) {
        setTraceId("standardId-"+standardId);
        return standardService.getStandardById(standardId);
    }

    @DeleteMapping("/{standardId}")
    public ResponseEntity<Object> deleteStandard(@PathVariable int standardId) {
        setTraceId("standardId-"+standardId);
        return standardService.deleteStandard(standardId);
    }

    @PatchMapping("/{standardId}")
    public ResponseEntity<Object> modifyStandard(@RequestBody StandardRequest standardRequest, @PathVariable int standardId) {
        setTraceId("standardId-"+standardId);
        return standardService.modifyStandard(standardRequest, standardId);
    }
}