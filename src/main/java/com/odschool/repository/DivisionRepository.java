package com.odschool.repository;

import com.odschool.entity.DivisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<DivisionEntity, Integer> {
    DivisionEntity findByDivisionAndStandardEntity_Id(char division, int standardId);
    List<DivisionEntity> findAllByStandardEntityId(int standardId);
}
