package com.odschool.repository;

import com.odschool.entity.SchoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, Integer> {
    public SchoolEntity findBySchoolName(String schoolName);
}
