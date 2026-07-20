package com.odschool.repository;

import com.odschool.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Integer> {
    StudentEntity findByStudentNameAndDivisionEntity_Id(String studentName, int divisionId);

}
