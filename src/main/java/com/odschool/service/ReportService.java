package com.odschool.service;

import com.odschool.dtos.ApiResponse;
import com.odschool.dtos.SearchRequest;
import com.odschool.dtos.StandardRequest;
import com.odschool.dtos.StudentResponse;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.entity.StudentEntity;
import com.odschool.enums.SearchStatus;
import com.odschool.interfaces.MapInterface;
import com.odschool.repository.DivisionRepository;
import com.odschool.repository.StandardRepository;
import com.odschool.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {
    @Autowired
    StandardRepository standardRepository;
    @Autowired
    DivisionRepository divisionRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    MapInterface mapInterface;
    //Top Tree Student Of Standard
    public ResponseEntity<Object> getTopThreeOfStandard(int standardId){
        StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
        if(standardEntity == null){
            ApiResponse response = new ApiResponse(
                    "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<DivisionEntity> divisionEntityList = divisionRepository.findByStandardEntityId(standardEntity.getId());

        List<StudentEntity> studentEntityList = studentRepository.findByDivisionEntityIdIn(divisionEntityList.stream().map(DivisionEntity::getId).toList());

        List<StudentResponse> topThreeStudent = studentEntityList.stream()
                                .filter(stud -> stud.getResult().equalsIgnoreCase("pass"))
                                .sorted(Comparator.comparing(StudentEntity::getPercentage))
                                .limit(1)
                                .map(stud -> mapInterface.toStudentResponse(stud))
                                .toList();
        ApiResponse response = new ApiResponse(topThreeStudent, "Top Three Student Of Standard: "+standardEntity.getStandard(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Top Tree Student Of Division
    public ResponseEntity<Object> getTopThreeOfDivision(int divisionId){
        DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
        if(divisionEntity == null){
            ApiResponse response = new ApiResponse(
                    "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<StudentEntity> studentEntityList = studentRepository.findAllByDivisionEntityId(divisionId);

        List<StudentResponse> topThreeStudent = studentEntityList.stream()
                .filter(stud -> stud.getResult().equalsIgnoreCase("pass"))
                .sorted(Comparator.comparing(StudentEntity::getPercentage))
                .limit(2)
                .map(stud -> mapInterface.toStudentResponse(stud))
                .toList();
        ApiResponse response = new ApiResponse(topThreeStudent, "Top Three Student Of Division: "+divisionEntity.getDivision(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    Average Pass Students of Standard
    public ResponseEntity<Object> getAveragePassedStudentsOfStandard(int standardId){
        StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
        if(standardEntity == null){
            ApiResponse response = new ApiResponse(
                    "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        List<DivisionEntity> divisionEntityList = divisionRepository.findByStandardEntityId(standardEntity.getId());
        List<StudentEntity> studentEntityList = studentRepository.findByDivisionEntityIdIn(divisionEntityList.stream().map(DivisionEntity::getId).toList());
        long totalStudent =(long) studentEntityList.size();
        long totalPassedStudent = studentEntityList.stream()
                .filter(stud -> stud.getResult().equalsIgnoreCase("pass"))
                .count();
        float avgPassedStudent = ((float) totalPassedStudent / (float) totalStudent) * (float) 100;
        ApiResponse response = new ApiResponse(avgPassedStudent, "Average Passed Student Of Standard: "+standardEntity.getStandard(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Average Failed Students Of Standard
    public ResponseEntity<Object> getAverageFailedStudentsOfStandard(int standardId){
        StandardEntity standardEntity = standardRepository.findById(standardId).orElse(null);
        if(standardEntity == null){
            ApiResponse response = new ApiResponse(
                    "STANDARD_NOT_FOUND_ERROR", "Standard not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        List<DivisionEntity> divisionEntityList = divisionRepository.findByStandardEntityId(standardEntity.getId());
        List<StudentEntity> studentEntityList = studentRepository.findByDivisionEntityIdIn(divisionEntityList.stream().map(DivisionEntity::getId).toList());
        long totalStudent =(long) studentEntityList.size();
        long totalFailedStudent = studentEntityList.stream()
                .filter(stud -> stud.getResult().equalsIgnoreCase("fail"))
                .count();
        float avgFailedStudent = ((float) totalFailedStudent / (float) totalStudent) * (float) 100;
        ApiResponse response = new ApiResponse(avgFailedStudent, "Average Failed Student Of Standard: "+standardEntity.getStandard(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Average Passed Students Of Division
    public ResponseEntity<Object> getAveragePassedStudentsOfDivision(int divisionId){
        DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
        if(divisionEntity == null){
            ApiResponse response = new ApiResponse(
                    "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        List<StudentEntity> studentEntityList = studentRepository.findAllByDivisionEntityId(divisionId);
        long totalStudent =(long) studentEntityList.size();
        long totalPassedStudent = studentEntityList.stream()
                .filter(stud -> stud.getResult().equalsIgnoreCase("pass"))
                .count();
        float avgPassedStudent = ((float) totalPassedStudent / (float) totalStudent) * (float) 100;
        ApiResponse response = new ApiResponse(avgPassedStudent, "Average Passed Student Of Division: "+divisionEntity.getDivision(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Average Failed Students Of Division
    public ResponseEntity<Object> getAverageFailedStudentsOfDivision(int divisionId){
        DivisionEntity divisionEntity = divisionRepository.findById(divisionId).orElse(null);
        if(divisionEntity == null){
            ApiResponse response = new ApiResponse(
                    "DIVISION_NOT_FOUND_ERROR", "Division not found", false, HttpStatus.NOT_FOUND.value()
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        List<StudentEntity> studentEntityList = studentRepository.findAllByDivisionEntityId(divisionId);
        long totalStudent =(long) studentEntityList.size();
        long totalFailedStudent = studentEntityList.stream()
                .filter(stud -> stud.getResult().equalsIgnoreCase("fail"))
                .count();
        float avgFailedStudent = ((float) totalFailedStudent / (float) totalStudent) * (float) 100;
        ApiResponse response = new ApiResponse(avgFailedStudent, "Average Failed Student Of Division: "+divisionEntity.getDivision(), true, HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Object> getRepost(SearchRequest request){
        if (request.getSearchStatus().equals(SearchStatus.TopThreeStandard)) {
            return getTopThreeOfStandard(request.getStandardId());
        } else if (request.getSearchStatus().equals(SearchStatus.TopThreeDivision)) {
            return getTopThreeOfDivision(request.getDivisionId());
        } else if (request.getSearchStatus().equals(SearchStatus.AvgPassStudentStandard)) {
            return  getAveragePassedStudentsOfStandard(request.getStandardId());
        } else if (request.getSearchStatus().equals(SearchStatus.AvgPassStudentDivision)) {
            return getAveragePassedStudentsOfDivision(request.getDivisionId());
        }else if (request.getSearchStatus().equals(SearchStatus.AvgFailStudentStandard)) {
            return getAverageFailedStudentsOfStandard(request.getStandardId());
        }else if (request.getSearchStatus().equals(SearchStatus.AvgFailStudentDivision)) {
            return getAverageFailedStudentsOfDivision(request.getDivisionId());
        }
        else {
            ApiResponse response = new ApiResponse("UNKNOWN_SEARCH", "Please Search Valid Report", false, HttpStatus.OK.value());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}
