package com.odschool.repository;

import com.odschool.entity.StandardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandardRepository extends JpaRepository<StandardEntity, Integer> {
    StandardEntity findByStandardAndSchoolEntity_Id(int standard, int schoolId);
}
