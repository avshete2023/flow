package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Trigger handler for webhook trigger configuration.
 */
@Component
public class WebhookTriggerHandler implements TriggerHandler {

    private static final Pattern WEBHOOK_IDENTIFIER_PATTERN = Pattern.compile("^wh_[a-f0-9]{32}$");

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.WEBHOOK;
    }

    @Override
    public TriggerValidationModel validateConfiguration(JsonNode configuration) {
        List<String> errors = new ArrayList<>();

        if (configuration == null || configuration.isNull() || !configuration.isObject()) {
            errors.add("Webhook configuration must be a JSON object");
            return TriggerValidationModel.failure(errors);
        }

        String webhookIdentifier = text(configuration.get("webhookIdentifier"));
        if (webhookIdentifier == null) {
            errors.add("Webhook identifier is required");
        } else if (!WEBHOOK_IDENTIFIER_PATTERN.matcher(webhookIdentifier).matches()) {
            errors.add("Webhook identifier format is invalid");
        }

        String secretToken = text(configuration.get("secretToken"));
        if (secretToken == null) {
            errors.add("Secret token is required");
        } else if (secretToken.length() < 16 || secretToken.length() > 128) {
            errors.add("Secret token must be between 16 and 128 characters");
        }

        JsonNode active = configuration.get("active");
        if (active != null && !active.isBoolean()) {
            errors.add("Active flag must be boolean");
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }

    private String text(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }
}

