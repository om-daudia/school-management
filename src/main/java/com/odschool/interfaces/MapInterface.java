package com.odschool.interfaces;

import com.odschool.dtos.*;
import com.odschool.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapInterface {
    SchoolResponse toSchoolResponse(SchoolEntity school);
    SchoolEntity toSchoolEntity(SchoolResponse schoolResponse);
    @Mapping(target = "schoolId", source = "schoolEntity.id")
    StandardResponse toStandardResponse(StandardEntity standard);
    StandardEntity toStandardEntity(StandardRequest standardRequest);

    @Mapping(target = "standardId", source = "standardEntity.id")
    DivisionResponse toDivisionResponse(DivisionEntity division);
    DivisionEntity toDivisionEntity(DivisionRequest divisionRequest);

    @Mapping(target = "divisionId", source = "divisionEntity.id")
    StudentResponse toStudentResponse(StudentEntity student);
    StudentEntity toStudentEntity(StudentRequest studentRequest);

    @Mapping(target = "studentId", source = "studentEntity.id")
    SubjectMarkResponse toSubjectMarkResponse(SubjectMarkEntity subjectMark);
    SubjectMarkEntity toSubjectMarkEntity(SubjectMarkRequest subjectMarkRequest);

}