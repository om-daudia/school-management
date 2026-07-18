package com.odschool.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "school")
@Data
public class SchoolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String schoolName;

    public SchoolEntity(String schoolName) {
        this.schoolName = schoolName;
    }
    public SchoolEntity() {
    }
}
