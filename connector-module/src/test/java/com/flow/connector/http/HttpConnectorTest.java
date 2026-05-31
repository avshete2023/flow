package com.flow.connector.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class HttpConnectorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldExecuteHttpRequestSuccessfully() throws Exception {
        server.createContext("/success", exchange -> writeJson(exchange, 200, "{\"ok\":true}"));

        HttpConnector connector = new HttpConnector(RestClient.builder(), objectMapper);
        JsonNode configuration = objectMapper.createObjectNode()
                .put("url", "http://localhost:" + port + "/success")
                .put("method", "GET")
                .put("timeoutMillis", 1500);

        ConnectorExecutionResult result = connector.execute(configuration, null);

        assertThat(result.success()).isTrue();
        assertThat(result.output().path("status").asText()).isEqualTo("SUCCESS");
        assertThat(result.output().path("body").asText()).contains("ok");
    }

    @Test
    void shouldReturnFailureForTimeout() throws Exception {
        server.createContext("/timeout", exchange -> {
            try {
                Thread.sleep(250L);
                writeJson(exchange, 200, "{\"ok\":true}");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                writeJson(exchange, 500, "{\"error\":\"interrupted\"}");
            }
        });

        HttpConnector connector = new HttpConnector(RestClient.builder(), objectMapper);
        JsonNode configuration = objectMapper.createObjectNode()
                .put("url", "http://localhost:" + port + "/timeout")
                .put("method", "GET")
                .put("timeoutMillis", 50);

        ConnectorExecutionResult result = connector.execute(configuration, null);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).containsIgnoringCase("timeout");
    }

    @Test
    void shouldReturnFailureForConnectionIssue() {
        HttpConnector connector = new HttpConnector(RestClient.builder(), objectMapper);
        JsonNode configuration = objectMapper.createObjectNode()
                .put("url", "http://localhost:1/unreachable")
                .put("method", "GET")
                .put("timeoutMillis", 100);

        ConnectorExecutionResult result = connector.execute(configuration, null);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).containsIgnoringCase("failed");
    }

    private void writeJson(HttpExchange exchange, int statusCode, String payload) throws IOException {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}

