package com.aycan.agentbackend.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FlightGatewayService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flight.api.base-url}")
    private String baseUrl;

    @Value("${flight.api.username}")
    private String username;

    @Value("${flight.api.password}")
    private String password;

    public FlightGatewayService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode queryFlights(String from, String to, String date) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/flights/search")
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("departureDate", date)
                .queryParam("numberOfPeople", 1)
                .toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Flight service returned an empty or invalid response.");
        }

        return objectMapper.readTree(response.getBody());
    }

    public JsonNode createTicket(long flightId, String passengerName) throws Exception {
        String token = loginAndGetToken();

        String url = baseUrl + "/api/v1/tickets";

        String requestBody = objectMapper.createObjectNode()
                .put("flightId", flightId)
                .put("ticketNumber", "AUTO-" + System.currentTimeMillis())
                .put("status", "BOOKED")
                .put("passengerName", passengerName)
                .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Ticket service returned an empty or invalid response.");
        }

        return objectMapper.readTree(response.getBody());
    }

    public JsonNode checkIn(long ticketId) throws Exception {
        String token = loginAndGetToken();

        String url = baseUrl + "/api/v1/checkin/" + ticketId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Check-in service returned an empty or invalid response.");
        }

        return objectMapper.readTree(response.getBody());
    }

    private String loginAndGetToken() throws Exception {
        String url = baseUrl + "/api/v1/auth/login";

        String requestBody = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Login response is empty");
        }

        JsonNode json = objectMapper.readTree(response.getBody());

        if (json.has("token")) {
            return json.get("token").asText();
        }

        if (json.has("accessToken")) {
            return json.get("accessToken").asText();
        }

        throw new RuntimeException("Token field not found in login response.");
    }
}