package com.odschool.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subjectmark")
@Data
public class SubjectMarkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String subjectName;
    private int marks;
    @ManyToOne()
    @JoinColumn(name = "student_id")
    private StudentEntity studentEntity;
}
