package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.registry.InMemoryTriggerRegistry;
import com.flow.connector.registry.TriggerRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service that delegates trigger validation to the handler registered for each trigger type.
 */
@Service
public class TriggerValidationService {

    private final TriggerRegistry triggerRegistry;

    public TriggerValidationService(TriggerRegistry triggerRegistry) {
        this.triggerRegistry = triggerRegistry;
    }

    public TriggerValidationModel validate(TriggerDefinition triggerDefinition) {
        if (triggerDefinition == null) {
            return TriggerValidationModel.failure(List.of("Trigger definition is required"));
        }

        List<String> errors = new ArrayList<>();

        TriggerValidationModel baseValidation = triggerDefinition.validate();
        if (!baseValidation.valid()) {
            errors.addAll(baseValidation.errors());
        }

        TriggerType triggerType = triggerDefinition.triggerType();
        if (triggerType == null) {
            return TriggerValidationModel.failure(errors);
        }

        TriggerValidationModel triggerValidation = validate(triggerType, triggerDefinition.configuration());

        if (!triggerValidation.valid()) {
            errors.addAll(triggerValidation.errors());
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }

    public TriggerValidationModel validate(TriggerType triggerType, JsonNode configuration) {
        if (triggerType == null) {
            return TriggerValidationModel.failure(List.of("Trigger type is required"));
        }

        try {
            return triggerRegistry.get(triggerType).validateConfiguration(configuration);
        } catch (InMemoryTriggerRegistry.UnknownTriggerTypeException exception) {
            return TriggerValidationModel.failure(List.of(exception.getMessage()));
        }
    }
}

