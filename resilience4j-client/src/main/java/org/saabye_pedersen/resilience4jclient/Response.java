package org.saabye_pedersen.resilience4jclient;

class Response {
    private final String payload;
    private final int statusCode;

    Response(String payload, int statusCode) {
        this.payload = payload;
        this.statusCode = statusCode;
    }

    String getPayload() {
        return payload;
    }

    int getStatusCode() {
        return statusCode;
    }
}
