package com.odschool.controller;

import com.odschool.dtos.SchoolRequest;
import com.odschool.dtos.SchoolResponse;
import com.odschool.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.odschool.util.TraceIdUtil.setTraceId;

@RestController
@RequestMapping("/odschool/school")
public class SchoolController {
    @Autowired
    SchoolService schoolService;
    @GetMapping()
    public ResponseEntity<Object> getAllSchools(){
        return schoolService.getAllSchools();
    }
    @PostMapping()
    public ResponseEntity<Object> addSchool(@RequestBody SchoolRequest schoolRequest){
        return schoolService.addSchool(schoolRequest);
    }
    @GetMapping("/{schoolId}")
    public ResponseEntity<Object> getSchoolById(@PathVariable int schoolId){
        setTraceId("schoolId-"+schoolId);
        return schoolService.getSchoolById(schoolId);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{schoolId}")
    public ResponseEntity<Object> deleteSchool(@PathVariable int schoolId){
        setTraceId("schoolId-"+schoolId);
        return schoolService.deleteSchool(schoolId);
    }
//    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{schoolId}")
    public ResponseEntity<Object> modifyschool(@RequestBody SchoolResponse schoolResponseDto, @PathVariable int schoolId){
        setTraceId("schoolId-"+schoolId);
        return schoolService.modifySchool(schoolResponseDto, schoolId);
    }
}
