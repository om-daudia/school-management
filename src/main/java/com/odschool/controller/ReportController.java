package com.odschool.controller;

import com.odschool.dtos.SearchRequest;
import com.odschool.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.odschool.util.TraceIdUtil.setTraceId;

@RestController
@RequestMapping("/odschool/report")
public class ReportController {
    @Autowired
    ReportService reportService;

    @GetMapping("/top-three-student-of-standardId/{standardId}")
    public ResponseEntity<Object> getTopThreeStudentOfStandard(@PathVariable int standardId){
        setTraceId("standardId-"+standardId);
        return reportService.getTopThreeOfStandard(standardId);
    }
    @GetMapping("/top-three-student-of-divisionId/{divisionId}")
    public ResponseEntity<Object> getTopThreeStudentOfDivision(@PathVariable int divisionId){
        setTraceId("divisionId-"+divisionId);
        return reportService.getTopThreeOfDivision(divisionId);
    }
    @GetMapping("/average-passed-student-of-standardId/{standardId}")
    public ResponseEntity<Object> getAveragePassedStudentsOfStandard(@PathVariable int standardId){
        setTraceId("standardId-"+standardId);
        return reportService.getAveragePassedStudentsOfStandard(standardId);
    }
    @GetMapping("/average-failed-student-of-standardId/{standardId}")
    public ResponseEntity<Object> getAverageFailedStudentsOfStandard(@PathVariable int standardId){
        setTraceId("standardId-"+standardId);
        return reportService.getAverageFailedStudentsOfStandard(standardId);
    }
    @GetMapping("/average-passed-student-of-divisionId/{divisionId}")
    public ResponseEntity<Object> getPassedStudentsOfDivision(@PathVariable int divisionId){
        setTraceId("divisionId-"+divisionId);
        return reportService.getAveragePassedStudentsOfDivision(divisionId);
    }
    @GetMapping("/average-failed-student-of-divisionId/{divisionId}")
    public ResponseEntity<Object> getFailedStudentsOfDivision(@PathVariable int divisionId){
        setTraceId("divisionId-"+divisionId);
        return reportService.getAverageFailedStudentsOfDivision(divisionId);
    }

    @PostMapping()
    public ResponseEntity<Object> getReports(@RequestBody SearchRequest request) {
        setTraceId("standardId-"+(request.getStandardId() > 0 ? request.getDivisionId() : request.getStandardId()));
        return reportService.getRepost(request);
    }
}
