package com.flow.connector.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.connector.connector.ConnectorValidationModel;
import org.junit.jupiter.api.Test;

class HttpConnectorConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSupportUrlMethodHeadersBodyAndTimeout() {
        var configurationJson = objectMapper.createObjectNode();
        configurationJson.put("url", "https://example.com/orders");
        configurationJson.put("method", "POST");
        configurationJson.set("headers", objectMapper.createObjectNode().put("Authorization", "Bearer token"));
        configurationJson.set("body", objectMapper.createObjectNode().put("orderId", "ORD-1"));
        configurationJson.put("timeoutMillis", 3000);

        HttpConnectorConfiguration configuration = HttpConnectorConfiguration.fromJson(configurationJson);

        ConnectorValidationModel validation = configuration.validate();

        assertThat(validation.valid()).isTrue();
        assertThat(configuration.url()).isEqualTo("https://example.com/orders");
        assertThat(configuration.method()).isEqualTo(HttpMethod.POST);
        assertThat(configuration.headers()).containsEntry("Authorization", "Bearer token");
        assertThat(configuration.body().path("orderId").asText()).isEqualTo("ORD-1");
        assertThat(configuration.timeoutMillis()).isEqualTo(3000);
    }

    @Test
    void shouldRequireUrlAndHttpMethod() {
        HttpConnectorConfiguration configuration = HttpConnectorConfiguration.fromJson(objectMapper.createObjectNode()
                .put("timeoutMillis", 1000));

        ConnectorValidationModel validation = configuration.validate();

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("URL is required", "HTTP method is required");
    }

    @Test
    void shouldRejectNonPositiveTimeout() {
        HttpConnectorConfiguration configuration = HttpConnectorConfiguration.fromJson(objectMapper.createObjectNode()
                .put("url", "https://example.com")
                .put("method", "GET")
                .put("timeoutMillis", 0));

        ConnectorValidationModel validation = configuration.validate();

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("Timeout must be greater than zero");
    }

    @Test
    void shouldTreatInvalidMethodAsMissingMethod() {
        HttpConnectorConfiguration configuration = HttpConnectorConfiguration.fromJson(objectMapper.createObjectNode()
                .put("url", "https://example.com")
                .put("method", "INVALID"));

        ConnectorValidationModel validation = configuration.validate();

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("HTTP method is required");
    }
}


