package com.dj.tennis_court.domain.court.service;

import com.dj.tennis_court.domain.court.domain.CourtDay;
import com.dj.tennis_court.domain.court.domain.CourtTime;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
        fetchCourt();
    }

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void fetchCourt() {
        for (int i = 1; i <= 8; i++) {
            fetchReserveStatusByCourt(Integer.toString(i));
        }
    }

    private void fetchReserveStatusByCourt(String stadiumIdx) {

        String targetUri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("stadiumIdx", Integer.parseInt(stadiumIdx) + 230)
                .build()
                .toUriString();

        String jsonString = restClient.get()
                .uri(targetUri)
                .retrieve()
                .body(String.class);

        // 가능 : null, 완료 : AP, 불가능 : CLOSE, NONE 고려

        try {
            PublicReserveResponse response = objectMapper.readValue(jsonString, PublicReserveResponse.class);

            if (response.getUseCntList() == null) {
                return;
            }

            List<CourtDay> existingDays = courtRepository.findByStadiumIdx(stadiumIdx);
            Map<LocalDate, CourtDay> dayMap = existingDays.stream()
                    .collect(Collectors.toMap(CourtDay::getSorDate, day -> day));

            for (PublicReserveResponse.CourtUseCount dto : response.getUseCntList()) {
                LocalDate sorDate = LocalDate.parse(dto.getSorDate());

                CourtDay courtDay = dayMap.get(sorDate);

                if (courtDay == null) {
                    courtDay = courtRepository.save(new CourtDay(stadiumIdx, sorDate));
                    dayMap.put(sorDate, courtDay);
                }

                String stadiumBeginHm = dto.getStadiumBeginHm();
                String applyStatusCd = dto.getApplyStatusCd() == null ? "AVAILABLE" : dto.getApplyStatusCd();

                CourtTime courtTime = new CourtTime(stadiumBeginHm, applyStatusCd);
                courtDay.addCourtTime(courtTime);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public List<CourtDay> getCourtDaysByStadium(String stadiumIdx) {
        return courtRepository.findByStadiumIdx(stadiumIdx);
    }
}
