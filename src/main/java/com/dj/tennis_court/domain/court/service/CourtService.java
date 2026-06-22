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
import java.util.Comparator;
import java.util.List;
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
        for (int i = 0; i <= 8; i++) {
            if (i == 4) continue;
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

        try {
            int stadiumIdxToInteger = Integer.parseInt(stadiumIdx);
            if (stadiumIdxToInteger < 4) {
                stadiumIdx = String.valueOf(Integer.parseInt(stadiumIdx) + 1);
            }
            PublicReserveResponse response = objectMapper.readValue(jsonString, PublicReserveResponse.class);

            LocalDate today = LocalDate.now();

            List<PublicReserveResponse.CourtUseCount> useCntList = response.getUseCntList();

            if (response.getUseCntList() == null) {
                return;
            }

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

                courtRepository.save(new Court(stadiumIdx, sorDate, stadiumBeginHm, stadiumEndHm));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public List<Court> getAvailableCourts(String stadiumIdx) {

        return courtRepository.findByStadiumIdx(String .valueOf(Integer.parseInt(stadiumIdx) - 230))
                .stream()
                .sorted(Comparator.comparing(Court::getSorDate)
                .thenComparing(Court::getStadiumBeginHm))
                .collect(Collectors.toList());
    }
}
