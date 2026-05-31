package com.flow.connector.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorMetadata;
import com.flow.connector.connector.ConnectorType;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * HTTP connector execution service based on Spring RestClient.
 */
@Component
public class HttpConnector implements Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);
    private static final Set<String> SENSITIVE_HEADER_NAMES = Set.of(
            "authorization",
            "x-api-key",
            "api-key",
            "apikey",
            "token",
            "secret"
    );

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public HttpConnector(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public ConnectorValidationModel validate(JsonNode configuration) {
        return HttpConnectorConfiguration.fromJson(configuration).validate();
    }

    @Override
    public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
        HttpConnectorConfiguration parsed = HttpConnectorConfiguration.fromJson(configuration);
        ConnectorValidationModel validation = parsed.validate();
        if (!validation.valid()) {
            return ConnectorExecutionResult.failure("Invalid HTTP connector configuration: " + String.join("; ", validation.errors()));
        }

        LOGGER.info(
                "Executing HTTP connector method={} url={} headers={}",
                parsed.method(),
                parsed.url(),
                maskSensitiveHeaders(parsed.headers())
        );

        try {
            RestClient restClient = buildRestClient(parsed.timeoutMillis());
            RestClient.RequestBodySpec request = restClient
                    .method(org.springframework.http.HttpMethod.valueOf(parsed.method().name()))
                    .uri(parsed.url())
                    .headers(httpHeaders -> parsed.headers().forEach(httpHeaders::add));

            RestClient.ResponseSpec response = parsed.body() == null
                    ? request.retrieve()
                    : request.body(parsed.body()).retrieve();

            String responseBody = response.body(String.class);
            ObjectNode output = objectMapper.createObjectNode()
                    .put("status", "SUCCESS")
                    .put("method", parsed.method().name())
                    .put("url", parsed.url())
                    .put("body", responseBody == null ? "" : responseBody);

            return ConnectorExecutionResult.success(output);
        } catch (ResourceAccessException exception) {
            return ConnectorExecutionResult.failure("HTTP call failed due to timeout/connectivity issue: " + exception.getMessage());
        } catch (RestClientException exception) {
            return ConnectorExecutionResult.failure("HTTP call failed: " + exception.getMessage());
        }
    }

    @Override
    public ConnectorMetadata getMetadata() {
        return new ConnectorMetadata(
                ConnectorType.HTTP,
                "HTTP Connector",
                "Executes outbound HTTP requests"
        );
    }

    private RestClient buildRestClient(Integer timeoutMillis) {
        if (timeoutMillis == null) {
            return restClientBuilder.build();
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    private Map<String, String> maskSensitiveHeaders(Map<String, String> headers) {
        Map<String, String> masked = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String lowerCaseKey = entry.getKey().toLowerCase(Locale.ROOT);
            masked.put(entry.getKey(), isSensitiveHeader(lowerCaseKey) ? "****" : entry.getValue());
        }
        return masked;
    }

    private boolean isSensitiveHeader(String lowerCaseHeaderName) {
        if (SENSITIVE_HEADER_NAMES.contains(lowerCaseHeaderName)) {
            return true;
        }
        return lowerCaseHeaderName.contains("token") || lowerCaseHeaderName.contains("secret");
    }
}

