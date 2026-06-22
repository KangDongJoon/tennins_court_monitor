package com.dj.tennis_court.domain.court.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublicReserveResponse {
    private String lastPoint;
    private List<CourtUseCount> useCntList;

    @Getter
    @Setter
    public static class CourtUseCount {
        private String sorDate;
        private String applyStatusCd;
        private String stadiumBeginHm;
        private String stadiumEndHm;
    }
}