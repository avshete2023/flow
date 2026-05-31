package com.flow.connector.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP connector configuration model.
 */
public record HttpConnectorConfiguration(
        String url,
        HttpMethod method,
        Map<String, String> headers,
        JsonNode body,
        Integer timeoutMillis
) {

    public static HttpConnectorConfiguration fromJson(JsonNode configuration) {
        if (configuration == null || !configuration.isObject()) {
            return new HttpConnectorConfiguration(null, null, Map.of(), null, null);
        }

        String url = text(configuration.get("url"));
        HttpMethod method = parseMethod(configuration.get("method"));
        Map<String, String> headers = parseHeaders(configuration.get("headers"));
        JsonNode body = configuration.get("body");
        Integer timeoutMillis = parseTimeout(configuration.get("timeoutMillis"));

        return new HttpConnectorConfiguration(url, method, headers, body, timeoutMillis);
    }

    public ConnectorValidationModel validate() {
        List<String> errors = new ArrayList<>();

        if (url == null || url.isBlank()) {
            errors.add("URL is required");
        }
        if (method == null) {
            errors.add("HTTP method is required");
        }
        if (timeoutMillis != null && timeoutMillis <= 0) {
            errors.add("Timeout must be greater than zero");
        }

        return errors.isEmpty() ? ConnectorValidationModel.success() : ConnectorValidationModel.failure(errors);
    }

    public ObjectNode toJson() {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        if (url != null) {
            root.put("url", url);
        }
        if (method != null) {
            root.put("method", method.name());
        }

        ObjectNode headerNode = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerNode.put(entry.getKey(), entry.getValue());
        }
        root.set("headers", headerNode);

        if (body != null) {
            root.set("body", body);
        }
        if (timeoutMillis != null) {
            root.put("timeoutMillis", timeoutMillis);
        }

        return root;
    }

    private static HttpMethod parseMethod(JsonNode methodNode) {
        String value = text(methodNode);
        if (value == null) {
            return null;
        }
        try {
            return HttpMethod.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Map<String, String> parseHeaders(JsonNode headersNode) {
        if (headersNode == null || !headersNode.isObject()) {
            return Map.of();
        }

        Map<String, String> headers = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isTextual()) {
                headers.put(field.getKey(), field.getValue().asText());
            }
        }
        return Map.copyOf(headers);
    }

    private static Integer parseTimeout(JsonNode timeoutNode) {
        if (timeoutNode == null || timeoutNode.isNull()) {
            return null;
        }
        if (!timeoutNode.canConvertToInt()) {
            return null;
        }
        return timeoutNode.asInt();
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }
}

