package com.dj.tennis_court.domain.court.service;

import com.dj.tennis_court.domain.court.dto.PublicReserveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CourtService {
    private final static String BASE_URL = "https://yeyak.hscity.go.kr/stadium/stadiumReserveUseList.do";

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    public PublicReserveResponse fetchReserveStatusByCourt(String stadiumIdx) {

        String targetUri = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("stadiumIdx", stadiumIdx)
                .build()
                .toUriString();

        String jsonString = restClient.get()
                .uri(targetUri)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(jsonString, PublicReserveResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

}
