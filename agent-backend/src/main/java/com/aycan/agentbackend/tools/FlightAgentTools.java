package com.aycan.agentbackend.tools;

import com.aycan.agentbackend.gateway.FlightGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class FlightAgentTools {

    private final FlightGatewayService flightGatewayService;
    private final ObjectMapper objectMapper;

    public FlightAgentTools(FlightGatewayService flightGatewayService, ObjectMapper objectMapper) {
        this.flightGatewayService = flightGatewayService;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "query_flights", description = "Search flights by departure airport code, arrival airport code, and departure date in yyyy-MM-dd format")
    public String queryFlights(String from, String to, String date) throws Exception {
        JsonNode result = flightGatewayService.queryFlights(from, to, date);
        return objectMapper.writeValueAsString(result);
    }

    @Tool(name = "book_ticket", description = "Book a ticket by flight id and passenger name")
    public String bookTicket(Long flightId, String passengerName) throws Exception {
        JsonNode result = flightGatewayService.createTicket(flightId, passengerName);
        return objectMapper.writeValueAsString(result);
    }

    @Tool(name = "check_in", description = "Check in a passenger by ticket id")
    public String checkIn(Long ticketId) throws Exception {
        JsonNode result = flightGatewayService.checkIn(ticketId);
        return objectMapper.writeValueAsString(result);
    }
}