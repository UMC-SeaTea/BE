package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TastingNoteTypeRepository extends JpaRepository<TastingNoteType, Long> {
    Optional<TastingNoteType> findByCode(String code);
}
