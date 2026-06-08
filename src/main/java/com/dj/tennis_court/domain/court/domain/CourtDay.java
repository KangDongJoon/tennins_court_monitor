package com.dj.tennis_court.domain.court.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CourtDay {

    @Id
    @GeneratedValue
    private Long id;

    private String stadiumIdx;

    private LocalDate sorDate;

    @OneToMany(mappedBy = "courtDay", cascade = CascadeType.ALL)
    private List<CourtTime> timeList = new ArrayList<>();

    public CourtDay(String stadiumIdx, LocalDate sorDate) {
        this.stadiumIdx = stadiumIdx;
        this.sorDate = sorDate;
    }

    public void addCourtTime(CourtTime courtTime) {
        this.timeList.add(courtTime);
        courtTime.setCourtDay(this);
    }
}
