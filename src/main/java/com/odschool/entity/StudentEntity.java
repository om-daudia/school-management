package com.odschool.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student")
@Data
public class StudentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String studentName;
    private double obtainMarks;
    private double percentage;
    private String result;
    @ManyToOne
    @JoinColumn(name = "division_id")
    private DivisionEntity divisionEntity;

    @OneToMany(mappedBy = "studentEntity", cascade = CascadeType.ALL)
    private List<SubjectMarkEntity> subjectMarkEntityList = new ArrayList<>();
}