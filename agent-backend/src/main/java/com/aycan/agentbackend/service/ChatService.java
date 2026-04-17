package com.aycan.agentbackend.service;

import com.aycan.agentbackend.dto.ChatResponse;
import com.aycan.agentbackend.gateway.FlightGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final FlightGatewayService flightGatewayService;
    private final ObjectMapper objectMapper;

    private Long lastCreatedTicketId = null;
    private Long lastSelectedFlightId = null;
    private String lastPassengerName = "John Doe";

    public ChatService(ChatClient.Builder chatClientBuilder,
                       FlightGatewayService flightGatewayService,
                       ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.flightGatewayService = flightGatewayService;
        this.objectMapper = objectMapper;
    }

    public ChatResponse ask(String message) {
        try {
            JsonNode decision = decideIntentWithLlm(message);

            String intent = decision.path("intent").asText("text");

            switch (intent) {
                case "query_flights" -> {
                    String from = decision.path("from").asText("");
                    String to = decision.path("to").asText("");
                    String date = decision.path("date").asText("");

                    if (from.isBlank() || to.isBlank() || date.isBlank()) {
                        return ChatResponse.text("I need departure airport, arrival airport, and date to search flights.");
                    }

                    JsonNode result = flightGatewayService.queryFlights(from, to, date);

                    if (result.isArray() && !result.isEmpty()) {
                        JsonNode firstFlight = result.get(0);
                        if (firstFlight.has("id")) {
                            lastSelectedFlightId = firstFlight.get("id").asLong();
                        }
                    }

                    return ChatResponse.of("flightResults", result);
                }

                case "book_ticket" -> {
                    Long flightId;

                    if (decision.has("flightId") && !decision.get("flightId").isNull()) {
                        flightId = decision.get("flightId").asLong();
                    } else {
                        flightId = lastSelectedFlightId;
                    }

                    if (flightId == null) {
                        return ChatResponse.text("Please search flights first so I can know which flight to book.");
                    }

                    String passengerName = decision.path("passengerName").asText("").trim();
                    if (!passengerName.isBlank()) {
                        lastPassengerName = passengerName;
                    }

                    JsonNode bookingResult = flightGatewayService.createTicket(flightId, lastPassengerName);

                    if (bookingResult.has("id")) {
                        lastCreatedTicketId = bookingResult.get("id").asLong();
                    }

                    return ChatResponse.of("booking", bookingResult);
                }

                case "check_in" -> {
                    Long ticketId;

                    if (decision.has("ticketId") && !decision.get("ticketId").isNull()) {
                        ticketId = decision.get("ticketId").asLong();
                    } else {
                        ticketId = lastCreatedTicketId;
                    }

                    if (ticketId == null) {
                        return ChatResponse.text("No recently created ticket found. Please book a ticket first.");
                    }

                    JsonNode checkInResult = flightGatewayService.checkIn(ticketId);
                    return ChatResponse.of("checkin", checkInResult);
                }

                default -> {
    String reply = decision.path("reply").asText("").trim();

    if (reply.isBlank() || reply.equals("...")) {
        reply = chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    return ChatResponse.text(reply);
}
            }

        } catch (Exception e) {
            return ChatResponse.text("Backend error: " + e.getMessage());
        }
    }

    private JsonNode decideIntentWithLlm(String message) throws Exception {
        String prompt = """
                You are an intent and parameter extractor for a flight assistant system.

                Your task is to analyze the user's message and return ONLY valid JSON.

                Supported intents:
                1. query_flights
                2. book_ticket
                3. check_in
                4. text

                Rules:
                - Return ONLY JSON, with no markdown and no explanation.
                - If the user wants to search flights, use intent = "query_flights".
                - If the user wants to book a flight or ticket, use intent = "book_ticket".
                - If the user wants to perform check-in, use intent = "check_in".
                - Otherwise use intent = "text".
                - Airport codes must be:
                  Istanbul -> IST
                  Frankfurt -> FRA
                  Izmir -> ADB
                - Dates must be in yyyy-MM-dd format when possible.
                - If passenger name is not given, use null.
                - If flightId or ticketId is not explicitly provided, use null.
                - If intent is text, include a short helpful reply field.

                JSON formats:

                For query_flights:
                {
                  "intent": "query_flights",
                  "from": "IST",
                  "to": "FRA",
                  "date": "2026-05-10"
                }

                For book_ticket:
                {
                  "intent": "book_ticket",
                  "flightId": null,
                  "passengerName": null
                }

                For check_in:
                {
                  "intent": "check_in",
                  "ticketId": null
                }

                For text:
                {
                  "intent": "text",
                  "reply": "..."
                }

                User message:
                %s
                """.formatted(message);

        String raw = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return objectMapper.readTree(raw);
    }
}