package com.odschool.interfaces;

import com.odschool.dtos.*;
import com.odschool.entity.DivisionEntity;
import com.odschool.entity.SchoolEntity;
import com.odschool.entity.StandardEntity;
import com.odschool.entity.StudentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapInterface {
    SchoolResponse toSchoolResponse(SchoolEntity school);
    SchoolEntity toSchoolEntity(SchoolResponse schoolResponse);

    StandardResponse toStandardResponse(StandardEntity standard);
    StandardEntity toStandardEntity(StandardRequest standardRequest);

    DivisionResponse toDivisionResponse(DivisionEntity division);
    DivisionEntity toDivisionEntity(DivisionRequest divisionRequest);

    StudentResponse toStudentResponse(StudentEntity student);
    StudentEntity toStudentEntity(StudentRequest studentRequest);
}