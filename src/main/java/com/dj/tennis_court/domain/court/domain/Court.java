package com.dj.tennis_court.domain.court.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Court {
    @Id
    @GeneratedValue
    private Long id;
    private String stadiumIdx;
    private LocalDate sorDate;
    private String stadiumBeginHm;
    private String stadiumEndHm;

    public Court(String stadiumIdx, LocalDate sorDate, String stadiumBeginHm, String stadiumEndHm) {
        this.stadiumIdx = stadiumIdx;
        this.sorDate = sorDate;
        this.stadiumBeginHm = stadiumBeginHm;
        this.stadiumEndHm = stadiumEndHm;
    }
}
