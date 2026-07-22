package com.odschool.controller;

import com.odschool.dtos.SearchRequest;
import com.odschool.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/odschool/report")
public class ReportController {
    @Autowired
    ReportService reportService;

    @GetMapping("/top-three-student-of-standard/{standardId}")
    public ResponseEntity<Object> getTopThreeStudentOfStandard(@PathVariable int standardId){
        return reportService.getTopThreeOfStandard(standardId);
    }
    @GetMapping("/top-three-student-of-division/{divisionId}")
    public ResponseEntity<Object> getTopThreeStudentOfDivision(@PathVariable int divisionId){
        return reportService.getTopThreeOfDivision(divisionId);
    }
    @GetMapping("/average-passed-student-of-standard/{standardId}")
    public ResponseEntity<Object> getAveragePassedStudentsOfStandard(@PathVariable int standardId){
        return reportService.getAveragePassedStudentsOfStandard(standardId);
    }
    @GetMapping("/average-failed-student-of-standard/{standardId}")
    public ResponseEntity<Object> getAverageFailedStudentsOfStandard(@PathVariable int standardId){
        return reportService.getAverageFailedStudentsOfStandard(standardId);
    }
    @GetMapping("/average-passed-student-of-division/{divisionId}")
    public ResponseEntity<Object> getPassedStudentsOfDivision(@PathVariable int divisionId){
        return reportService.getAveragePassedStudentsOfDivision(divisionId);
    }
    @GetMapping("/average-failed-student-of-division/{divisionId}")
    public ResponseEntity<Object> getFailedStudentsOfDivision(@PathVariable int divisionId){
        return reportService.getAverageFailedStudentsOfDivision(divisionId);
    }

    @PostMapping()
    public ResponseEntity<Object> getReports(@RequestBody SearchRequest request) {
        return reportService.getRepost(request);
    }
}
