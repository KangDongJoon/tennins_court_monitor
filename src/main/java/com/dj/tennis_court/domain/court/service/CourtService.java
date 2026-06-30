package com.dj.tennis_court.domain.court.service;

import com.dj.tennis_court.domain.court.domain.Court;
import com.dj.tennis_court.domain.court.dto.PublicReserveResponse;
import com.dj.tennis_court.domain.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourtService {
    private final static String BASE_URL = "https://yeyak.hscity.go.kr/stadium/stadiumReserveUseList.do";

    private final CourtRepository courtRepository;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initFetch() {
        List<Court> courts = courtRepository.findAll();
        if (courts.isEmpty()) {
            fetchCourt();
        }
    }

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void fetchCourt() {
        for (int i = 0; i <= 8; i++) {
            if (i == 4) continue;
            fetchReserveStatusByCourt(Integer.toString(i));
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void removeExpiredCourt() {
        courtRepository.deleteCourtsBySorDateBefore(LocalDate.now());
    }

    private void fetchReserveStatusByCourt(String stadiumIdx) {
        LocalDate today = LocalDate.now();

        UriComponentsBuilder targetUri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("stadiumIdx", Integer.parseInt(stadiumIdx) + 230);


        String currentMonthUri = targetUri.build().toUriString();

        String jsonString = restClient.get()
                .uri(currentMonthUri)
                .retrieve()
                .body(String.class);

        checkCourt(stadiumIdx, jsonString, today);

        if (today.getDayOfMonth() >= 27) {
            LocalDate searchYearMonth = today.plusMonths(1);
            int searchYear = searchYearMonth.getYear();
            int searchMonth = searchYearMonth.getMonthValue();

            targetUri.replaceQueryParam("searchYear", searchYear)
                    .replaceQueryParam("searchMonth", searchMonth);

            String nextMonthUri = targetUri.build().toUriString();

            jsonString = restClient.get()
                    .uri(nextMonthUri)
                    .retrieve()
                    .body(String.class);

            checkCourt(stadiumIdx, jsonString, today);
        }
    }

    private void checkCourt(String stadiumIdx, String jsonString, LocalDate today) {
        try {
            int stadiumIdxToInteger = Integer.parseInt(stadiumIdx);
            if (stadiumIdxToInteger < 4) {
                stadiumIdx = String.valueOf(Integer.parseInt(stadiumIdx) + 1);
            }
            PublicReserveResponse response = objectMapper.readValue(jsonString, PublicReserveResponse.class);

            List<PublicReserveResponse.CourtUseCount> useCntList = response.getUseCntList();

            if (response.getUseCntList() == null) {
                return;
            }

            Set<String> dbCache = courtRepository.findByStadiumIdx(stadiumIdx).stream()
                    .map(c -> c.getSorDate() + c.getStadiumBeginHm())
                    .collect(Collectors.toSet());

            for (PublicReserveResponse.CourtUseCount courtTime : useCntList) {

                // 미래가 아닌 시점의 코트는 넘어감
                LocalDate sorDate = LocalDate.parse(courtTime.getSorDate());
                if (!sorDate.isAfter(today)) continue;

                // End - Begin 시간이 2시간 미만이면 예약 불가 코트이므로 넘어감
                String stadiumBeginHm = courtTime.getStadiumBeginHm();
                String stadiumEndHm = courtTime.getStadiumEndHm();
                Duration duration = Duration.between((LocalTime.parse(stadiumBeginHm)), LocalTime.parse(stadiumEndHm));
                if (duration.toHours() < 2) continue;

                // 예약 가능 상태가 아니면 조회 후 업데이트 없으면 넘어감
                String applyStatusCd = courtTime.getApplyStatusCd();
                if (applyStatusCd != null) {
                    courtRepository
                            .findByStadiumIdxAndSorDateAndStadiumBeginHm(stadiumIdx, sorDate, stadiumBeginHm)
                            .ifPresent(courtRepository::delete);

                    continue;
                }

                String key = sorDate + stadiumBeginHm;
                if (dbCache.contains(key)) {
                    continue;
                }

                courtRepository.save(new Court(stadiumIdx, sorDate, stadiumBeginHm, stadiumEndHm));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public List<Court> getAvailableCourts(String stadiumIdx) {

        return courtRepository.findByStadiumIdx(stadiumIdx)
                .stream()
                .sorted(Comparator.comparing(Court::getSorDate)
                        .thenComparing(Court::getStadiumBeginHm))
                .collect(Collectors.toList());
    }

    public List<LocalDate> makeCalendar(YearMonth currentMonth) {

        LocalDate startDate = currentMonth.atDay(1);
        int dayOfWeek = startDate.getDayOfWeek().getValue() % 7;
        LocalDate calendarStart = startDate.minusDays(dayOfWeek);

        LocalDate endDate = currentMonth.atEndOfMonth();
        int endDayOfWeek = endDate.getDayOfWeek().getValue() % 7;
        LocalDate calendarEnd = endDate.plusDays(6 - endDayOfWeek);

        List<LocalDate> dateList = new ArrayList<>();
        LocalDate iterDate = calendarStart;
        while (!iterDate.isAfter(calendarEnd)) {
            dateList.add(iterDate);
            iterDate = iterDate.plusDays(1);
        }

        return dateList;
    }
}
