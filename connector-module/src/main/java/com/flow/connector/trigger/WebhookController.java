package com.flow.connector.trigger;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * API endpoint for incoming webhook events.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/{workflowId}")
    public ResponseEntity<WebhookPayloadResponse> handleWebhook(
            @PathVariable UUID workflowId,
            @Valid @RequestBody WebhookPayloadRequest request
    ) {
        try {
            WebhookPayloadResponse response = webhookService.handleWebhook(workflowId, request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (WebhookService.WebhookConfigurationNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (WebhookService.InvalidWebhookSecretException | WebhookService.WebhookInactiveException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}


