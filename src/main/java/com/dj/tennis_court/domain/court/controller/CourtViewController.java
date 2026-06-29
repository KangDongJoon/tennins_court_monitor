package com.dj.tennis_court.domain.court.controller;

import com.dj.tennis_court.domain.court.domain.Court;
import com.dj.tennis_court.domain.court.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CourtViewController {

    private final CourtService courtService;

    @GetMapping({"/court-view", "/court-view/{stadiumIdx}"})
    public String showCourtPage(
            @PathVariable(value = "stadiumIdx", required = false) String stadiumIdx,
            @RequestParam(value = "searchYear", required = false) Integer searchYear,
            @RequestParam(value = "searchMonth", required = false) Integer searchMonth,
            Model model) {

        // default stadiumIdx
        if (stadiumIdx == null) {
            stadiumIdx = "1";
        }

        // make calendar
        LocalDate today = LocalDate.now();

        int viewYear = (searchYear != null) ? searchYear : today.getYear();
        int viewMonth = (searchMonth != null) ? searchMonth : today.getMonthValue();
        YearMonth currentYearMonth = YearMonth.of(viewYear, viewMonth);
        List<LocalDate> calendar = courtService.makeCalendar(currentYearMonth);

        List<Court> courts = courtService.getAvailableCourts(stadiumIdx);
        Map<String, List<Court>> groupedCourts = courts.stream()
                .collect(Collectors.groupingBy(
                        court -> court.getSorDate().toString(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        String selectedYearMonth = String.format("%04d-%02d", viewYear, viewMonth);

        model.addAttribute("stadiumIdx", stadiumIdx);
        model.addAttribute("calendar", calendar);
        model.addAttribute("groupedCourts", groupedCourts);
        model.addAttribute("selectedYearMonth", selectedYearMonth);
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("today", today);

        return "courts";
    }
}