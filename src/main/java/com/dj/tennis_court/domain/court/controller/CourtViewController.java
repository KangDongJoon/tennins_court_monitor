package com.dj.tennis_court.domain.court.controller;

import com.dj.tennis_court.domain.court.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CourtViewController {

    private final CourtService courtService;

    @GetMapping("/court-view")
    public String showCourtPage(@RequestParam("stadiumIdx") String stadiumIdx, Model model) {

        model.addAttribute("stadiumIdx", stadiumIdx);
        model.addAttribute("courts", courtService.getAvailableCourts(stadiumIdx));

        return "courts";
    }
}
