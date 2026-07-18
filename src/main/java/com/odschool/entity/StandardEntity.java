package com.odschool.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "standard")
@Data
public class StandardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int standard;
    @ManyToOne()
    @JoinColumn(name = "school_id")
    private SchoolEntity schoolEntity;

    @OneToMany(mappedBy = "standardEntity", cascade = CascadeType.ALL)
    private List<DivisionEntity> divisionEntityList;

}
