import { useEffect, useRef, useState } from "react";
import "./index.css";

function App() {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([
    {
      sender: "bot",
      type: "text",
      text: "Welcome aboard! I’m your AI Flight Assistant."
    },
    {
      sender: "bot",
      type: "text",
      text: "I can help you search flights, book tickets, and complete check-in. How can I help you today?"
    }
  ]);
  const [loading, setLoading] = useState(false);
  const [showTakeoff, setShowTakeoff] = useState(false);
  const chatBoxRef = useRef(null);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages, loading]);

  const formatDateTime = (value) => {
    if (!value) return "Not Available";

    const date = new Date(value);

    if (isNaN(date.getTime())) {
      return value.replace("T", " ");
    }

    return date.toLocaleString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  const sendMessage = async () => {
    if (!message.trim() || loading) return;

    const currentMessage = message;
    setMessage("");

    const userMessage = {
      sender: "user",
      type: "text",
      text: currentMessage
    };

    setMessages((prev) => [...prev, userMessage]);
    setLoading(true);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/chat`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ message: currentMessage })
      });

      const data = await response.json();

      if (data.type === "flightResults" && Array.isArray(data.data)) {
        setMessages((prev) => [
          ...prev,
          {
            sender: "bot",
            type: "flightResults",
            flights: data.data.map((flight) => ({
              code: flight.flightNumber || "N/A",
              from: flight.airportFrom || "N/A",
              to: flight.airportTo || "N/A",
              time: formatDateTime(flight.departureDateTime),
              duration: flight.durationMinutes
                ? `${flight.durationMinutes} min`
                : "N/A",
              status:
                flight.availableSeats !== undefined
                  ? (flight.availableSeats > 0
                      ? `${flight.availableSeats} seats left`
                      : "Full")
                  : "Available"
            }))
          }
        ]);
      } else if (data.type === "booking" && data.data) {
        const booking = data.data;

        setMessages((prev) => [
          ...prev,
          {
            sender: "bot",
            type: "booking",
            booking: {
              flight: booking.flight?.flightNumber || booking.flightNumber || "N/A",
              route: `${booking.flight?.airportFrom || booking.airportFrom || "N/A"} → ${booking.flight?.airportTo || booking.airportTo || "N/A"}`,
              time: `${formatDateTime(booking.flight?.departureDateTime || booking.departureDateTime)} - ${formatDateTime(booking.flight?.arrivalDateTime || booking.arrivalDateTime)}`,
              ticketNumber: booking.ticketNumber || "N/A",
              passenger: booking.passengerName || "John Doe",
              seat: booking.seatNumber || "Not assigned yet",
              purchaseTime: formatDateTime(booking.purchaseDateTime)
            }
          }
        ]);
      } else if (data.type === "checkin" && data.data) {
        const c = data.data;

        setMessages((prev) => [
          ...prev,
          {
            sender: "bot",
            type: "checkin",
            checkin: {
              flight: c.ticket?.flight?.flightNumber || c.flightNumber || "N/A",
              passenger: c.passenger?.fullName || c.passengerName || "John Doe",
              seat: c.seatNumber || "Assigned at airport",
              gate: c.gate || "To Be Announced",
              boardingTime: c.boardingTime || "At Gate Screen",
              status: c.checkedIn ? "Checked-in" : "Pending",
              ticketNumber: c.ticket?.ticketNumber || c.ticketNumber || "N/A"
            }
          }
        ]);

        setShowTakeoff(true);
        setTimeout(() => setShowTakeoff(false), 2500);
      } else {
        setMessages((prev) => [
          ...prev,
          {
            sender: "bot",
            type: "text",
            text: data.reply || "No response received."
          }
        ]);
      }
    } catch (error) {
      console.error("SEND MESSAGE ERROR:", error);

      setMessages((prev) => [
        ...prev,
        {
          sender: "bot",
          type: "text",
          text: "Connection problem. Please make sure the backend is running."
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !loading) {
      sendMessage();
    }
  };

  const fillPrompt = (text) => {
    if (!loading) {
      setMessage(text);
    }
  };

  const renderBotMessage = (msg) => {
    if (msg.type === "flightResults") {
      return (
        <div className="assistant-card">
          <div className="assistant-card-top">
            <span className="assistant-dot"></span>
            <span className="assistant-label">Available Flights</span>
          </div>

          <div className="flight-cards">
            {msg.flights.map((flight, index) => (
              <div className="flight-card" key={index}>
                <div className="flight-card-header">
                  <div className="flight-code">{flight.code}</div>
                  <div className="flight-status">{flight.status}</div>
                </div>

                <div className="flight-route">
                  <div className="airport-box">
                    <div className="airport-code">{flight.from}</div>
                    <div className="airport-label">Departure</div>
                  </div>

                  <div className="route-line">✈</div>

                  <div className="airport-box">
                    <div className="airport-code">{flight.to}</div>
                    <div className="airport-label">Arrival</div>
                  </div>
                </div>

                <div className="flight-meta">
                  <div className="flight-meta-row">
                    <span className="meta-title">Departure:</span>
                    <span>{flight.time}</span>
                  </div>
                  <div className="flight-meta-row">
                    <span className="meta-title">Duration:</span>
                    <span>{flight.duration}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      );
    }

    if (msg.type === "booking") {
      const b = msg.booking;
      return (
        <div className="assistant-card booking-card">
          <div className="assistant-card-top">
            <span className="assistant-dot"></span>
            <span className="assistant-label">Booking Confirmed</span>
          </div>

          <div className="ticket-box">
            <div className="ticket-main">{b.flight}</div>
            <div className="ticket-sub">{b.route}</div>
            <div className="ticket-detail">Time: {b.time}</div>
            <div className="ticket-detail">Ticket No: {b.ticketNumber}</div>
            <div className="ticket-detail">Passenger: {b.passenger}</div>
            <div className="ticket-detail">Seat: {b.seat}</div>
            <div className="ticket-detail">Purchased: {b.purchaseTime}</div>
          </div>
        </div>
      );
    }

    if (msg.type === "checkin") {
      const c = msg.checkin;
      return (
        <div className="assistant-card boarding-card">
          <div className="assistant-card-top">
            <span className="assistant-dot"></span>
            <span className="assistant-label">Check-In Complete</span>
          </div>

          <div className="boarding-box">
            <div className="boarding-title">{c.flight}</div>
            <div className="boarding-detail">Ticket No: {c.ticketNumber}</div>
            <div className="boarding-detail">Passenger: {c.passenger}</div>
            <div className="boarding-detail">Seat: {c.seat}</div>
            <div className="boarding-detail">Gate: {c.gate}</div>
            <div className="boarding-detail">Boarding Time: {c.boardingTime}</div>
            <div className="boarding-status">{c.status}</div>
          </div>
        </div>
      );
    }

    return (
      <div className="assistant-card">
        <div className="assistant-card-top">
          <span className="assistant-dot"></span>
          <span className="assistant-label">AI Flight Assistant</span>
        </div>
        <div className="assistant-text">{msg.text}</div>
      </div>
    );
  };

  return (
    <div className="app">
      <div className="background-overlay"></div>

      <div className="chat-shell">
        <div className="chat-header">
          <div>
            <p className="header-tag">Smart Travel Assistant</p>
            <h1>AI Flight Agent</h1>
            <p className="header-subtitle">
              Search flights, book tickets, and manage check-in with a modern airline assistant.
            </p>
          </div>
          <div className="plane-badge">✈</div>
        </div>

        <div className="quick-buttons">
          <button
            onClick={() => fillPrompt("Find a flight from Istanbul to Frankfurt on May 10")}
            disabled={loading}
          >
            Query Flight
          </button>
          <button
            onClick={() => fillPrompt("book")}
            disabled={loading}
          >
            Book Flight
          </button>
          <button
            onClick={() => fillPrompt("check in")}
            disabled={loading}
          >
            Check In
          </button>
        </div>

        <div className="chat-box" ref={chatBoxRef}>
          {messages.map((msg, index) => (
            <div
              key={index}
              className={`message-row ${msg.sender === "user" ? "user-row" : "bot-row"}`}
            >
              {msg.sender === "user" ? (
                <div className="message user">{msg.text}</div>
              ) : (
                renderBotMessage(msg)
              )}
            </div>
          ))}

          {loading && (
            <div className="message-row bot-row">
              <div className="assistant-card typing-card">
                <div className="assistant-card-top">
                  <span className="assistant-dot"></span>
                  <span className="assistant-label">AI Flight Assistant</span>
                </div>
                <div className="assistant-text">Thinking...</div>
              </div>
            </div>
          )}
        </div>

        {showTakeoff && (
          <div className="takeoff-animation">
            <span className="takeoff-plane">✈</span>
          </div>
        )}

        <div className="input-area">
          <input
            type="text"
            placeholder="Ask about flights, bookings, or check-in..."
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
          />
          <button onClick={sendMessage} disabled={loading}>
            {loading ? "Sending..." : "Send"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;