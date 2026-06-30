package com.dj.tennis_court.domain.court.repository;

import com.dj.tennis_court.domain.court.domain.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Long> {

    Optional<Court> findByStadiumIdxAndSorDateAndStadiumBeginHm(String stadiumIdx, LocalDate sorDate, String stadiumBeginHm);

    List<Court> findByStadiumIdx(String stadiumIdx);

    @Modifying
    void deleteCourtsBySorDateBefore(LocalDate now);
}
