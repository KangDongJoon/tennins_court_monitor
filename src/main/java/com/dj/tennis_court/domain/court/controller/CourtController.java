package com.dj.tennis_court.domain.court.controller;

import com.dj.tennis_court.domain.court.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/v1/courts/status")
    public String getCourtStatus(@RequestParam("stadiumIdx") String stadiumIdx) {
        return null;
    }
}

