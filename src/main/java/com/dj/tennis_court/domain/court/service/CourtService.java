package com.dj.tennis_court.domain.court.service;

import com.dj.tennis_court.domain.court.dto.PublicReserveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourtService {
    private final static String BASE_URL = "https://yeyak.hscity.go.kr/stadium/stadiumReserveUseList.do";

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

            for (PublicReserveResponse.CourtUseCount dto : response.getUseCntList()) {
                log.info("========================================");
                log.info("코트 번호: {}", stadiumIdx);
                log.info("분리된 날짜(Day): {}", dto.getSorDate());
                log.info("분리된 시간(Begin): {}", dto.getStadiumBeginHm());
                log.info("예약 상태(Status): {}", dto.getApplyStatusCd());
            }

            return "Data loaded successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

}
