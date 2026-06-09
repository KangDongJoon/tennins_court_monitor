package com.dj.tennis_court.domain.court.service;

import com.dj.tennis_court.domain.court.domain.CourtDay;
import com.dj.tennis_court.domain.court.domain.CourtTime;
import com.dj.tennis_court.domain.court.dto.PublicReserveResponse;
import com.dj.tennis_court.domain.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourtService {
    private final static String BASE_URL = "https://yeyak.hscity.go.kr/stadium/stadiumReserveUseList.do";

    private final CourtRepository courtRepository;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    public String fetchReserveStatusByCourt(String stadiumIdx) {

        String targetUri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("stadiumIdx", stadiumIdx)
                .build()
                .toUriString();

        String jsonString = restClient.get()
                .uri(targetUri)
                .retrieve()
                .body(String.class);

        // 가능 : null, 완료 : AP, 불가능 : CLOSE

        try {
            PublicReserveResponse response = objectMapper.readValue(jsonString, PublicReserveResponse.class);

            if (response.getUseCntList() == null) {
                return "Failed to load data.";
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

            return "Data loaded successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

}
