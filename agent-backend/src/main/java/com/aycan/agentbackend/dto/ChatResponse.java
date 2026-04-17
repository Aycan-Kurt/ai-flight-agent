package com.aycan.agentbackend.dto;

public class ChatResponse {

    private String type;
    private Object data;
    private String reply;

    public ChatResponse() {
    }

    public ChatResponse(String type, Object data, String reply) {
        this.type = type;
        this.data = data;
        this.reply = reply;
    }

    public static ChatResponse text(String reply) {
        return new ChatResponse("text", null, reply);
    }

    public static ChatResponse of(String type, Object data) {
        return new ChatResponse(type, data, null);
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String getReply() {
        return reply;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}