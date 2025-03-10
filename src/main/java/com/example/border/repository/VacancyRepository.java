package com.example.border.repository;

import com.example.border.model.entity.Vacancy;
import com.example.border.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID>, JpaSpecificationExecutor<Vacancy> {
    List<Vacancy> getVacanciesByPositionOrOtherPosition(Position position, String otherPosition);
}