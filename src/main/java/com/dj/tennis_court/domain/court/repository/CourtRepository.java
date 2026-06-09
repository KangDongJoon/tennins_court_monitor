package com.dj.tennis_court.domain.court.repository;

import com.dj.tennis_court.domain.court.domain.CourtDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<CourtDay, Long> {

    Optional<CourtDay> findByStadiumIdxAndSorDate(String stadiumIdx, LocalDate sorDate);

    List<CourtDay> findByStadiumIdx(String stadiumIdx);
}
