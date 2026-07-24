package com.odschool.controller;

import com.odschool.dtos.DivisionRequest;
import com.odschool.dtos.DivisionResponse;
import com.odschool.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("odschool/standardId/{standardId}/divisionId")
public class DivisionController {
    @Autowired
    DivisionService divisionService;

    @GetMapping()
    public ResponseEntity<Object> getAllDivisions(@PathVariable int standardId) {
        return divisionService.getAllDivisions(standardId);
    }

    @PostMapping()
    public ResponseEntity<Object> addDivision(@RequestBody DivisionRequest divisionRequest, @PathVariable int standardId) {
        return divisionService.addDivision(divisionRequest, standardId);
    }

    @GetMapping("/{divisionId}")
    public ResponseEntity<Object> getDivisionById(@PathVariable int divisionId) {
        return divisionService.getDivisionById(divisionId);
    }

    @DeleteMapping("/{divisionId}")
    public ResponseEntity<Object> deleteDivision(@PathVariable int divisionId) {
        return divisionService.deleteDivision(divisionId);
    }

    @PatchMapping("/{divisionId}")
    public ResponseEntity<Object> modifyDivision(@RequestBody DivisionResponse divisionResponseDto, @PathVariable int divisionId) {
        return divisionService.modifyDivision(divisionResponseDto, divisionId);
    }
}