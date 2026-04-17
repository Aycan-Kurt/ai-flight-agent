# Airline AI Flight Agent

## SE 4458 – Assignment 2
AI Agent Chat Application for Flight Services

---

## Student
Aycan Kurt

---

## Project Overview

This project is an AI-powered flight assistant developed for SE 4458 Assignment 2.

The system uses natural language processing to allow users to interact with flight services through a chat interface.

### Users Can:
- Search flights
- Book tickets
- Complete check-in operations
- Receive chatbot responses in real time

The project was developed based on the midterm flight APIs and follows the architecture requested in the assignment document.

---

## Main Features

### Query Flight
Users can search available flights using natural language.

Example:
```text
Find a flight from Istanbul to Frankfurt on May 10

Book Flight

Users can reserve a flight quickly.

Example:

book
Check-in

Users can complete check-in directly from the chat screen.

Example:

check in
General Chat Support

Example:

hello
System Architecture
1. flight-chat-ui

##Frontend developed with:

React
Vite
JavaScript
CSS
2. agent-backend

##Backend developed with:

Java
Spring Boot
3. mcp-flight-server

##Tool server developed with:

Java
Spring Boot

##Tools:

query_flights
book_ticket
check_in
AI Integration

##Used Technologies:

Ollama
llama3

The local LLM understands user intent and selects the correct tool.

##How to Run the Project
Start MCP Server
cd mcp-flight-server
mvn spring-boot:run
Start Backend
cd agent-backend
mvn spring-boot:run
Start Frontend
cd flight-chat-ui
npm install
npm run dev
Start Ollama
ollama run llama3

##Demo Video

https://youtu.be/h6Lxo0NN4P4

##Challenges Encountered
Connecting multiple services
Natural language understanding
UI state management
Tool routing logic

##Conclusion

This project successfully combines frontend, backend, AI integration, and real-time interaction in one intelligent flight assistant system