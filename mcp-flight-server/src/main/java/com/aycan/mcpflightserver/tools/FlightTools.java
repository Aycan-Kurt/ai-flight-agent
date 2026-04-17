package com.aycan.mcpflightserver.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FlightTools {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${flight.api.base-url}")
    private String baseUrl;

    @Value("${flight.api.username}")
    private String username;

    @Value("${flight.api.password}")
    private String password;

    public FlightTools(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "query_flights", description = "Search flights by departure city, arrival city, and departure date")
    public String queryFlights(String from, String to, String date) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/flights/search")
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("departureDate", date)
                .queryParam("numberOfPeople", 1)
                .toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Flight search response is empty or invalid.");
        }

        return response.getBody();
    }

    @Tool(name = "book_ticket", description = "Book a ticket by flight id and passenger name")
    public String bookTicket(Long flightId, String passengerName) throws Exception {
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
            throw new RuntimeException("Ticket booking response is empty or invalid.");
        }

        return response.getBody();
    }

    @Tool(name = "check_in", description = "Check in a passenger by ticket id")
    public String checkIn(Long ticketId) throws Exception {
        String token = loginAndGetToken();

        String url = baseUrl + "/api/v1/checkin/" + ticketId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new RuntimeException("Check-in response is empty or invalid.");
        }

        return response.getBody();
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
            throw new RuntimeException("Login response is empty.");
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