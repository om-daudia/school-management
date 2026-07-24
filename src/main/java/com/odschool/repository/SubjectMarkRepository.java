package com.odschool.repository;

import com.odschool.entity.SubjectMarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectMarkRepository extends JpaRepository<SubjectMarkEntity, Integer> {
    SubjectMarkEntity findBySubjectNameAndStudentEntity_Id(String subjectName, int studentId);
    List<SubjectMarkEntity> findAllByStudentEntity_Id(int studentId);

}
