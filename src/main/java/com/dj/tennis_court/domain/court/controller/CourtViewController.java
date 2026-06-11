package com.dj.tennis_court.domain.court.controller;

import com.dj.tennis_court.domain.court.domain.CourtDay;
import com.dj.tennis_court.domain.court.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourtViewController {

    private final CourtService courtService;

    @GetMapping("/court-view")
    public String showCourtPage(@RequestParam(value = "stadiumIdx", defaultValue = "231") String stadiumIdx, Model model) {
        List<CourtDay> courtDays = courtService.getCourtDaysByStadium(stadiumIdx);
        int stadiumIdxNum = Integer.parseInt(stadiumIdx) - 230;

        model.addAttribute("courtDays", courtDays);
        model.addAttribute("stadiumIdx", stadiumIdxNum);

        return "courts";
    }
}
