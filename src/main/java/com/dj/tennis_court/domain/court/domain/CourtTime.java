package com.dj.tennis_court.domain.court.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class CourtTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stadiumBeginHm;
    private String applyStatusCd;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_day_id")
    private CourtDay courtDay;

    public CourtTime(String stadiumBeginHm, String applyStatusCd) {
        this.stadiumBeginHm = stadiumBeginHm;
        this.applyStatusCd = applyStatusCd;
    }
}