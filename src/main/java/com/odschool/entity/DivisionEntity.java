package com.odschool.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "division")
@Data
public class DivisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private char division;
    @ManyToOne
    @JoinColumn(name = "standard_id")
    private StandardEntity standardEntity;

    @OneToMany(mappedBy = "divisionEntity", cascade = CascadeType.ALL)
    private List<StudentEntity> studentEntityList;
}