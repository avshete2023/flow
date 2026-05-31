package com.flow.connector.registry;

import com.flow.connector.trigger.TriggerHandler;
import com.flow.connector.trigger.TriggerType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * In-memory trigger registry populated from Spring-managed handlers.
 */
@Component
public class InMemoryTriggerRegistry implements TriggerRegistry {

    private final Map<TriggerType, TriggerHandler> handlersByType;

    public InMemoryTriggerRegistry(List<TriggerHandler> triggerHandlers) {
        this.handlersByType = new EnumMap<>(TriggerType.class);
        for (TriggerHandler handler : triggerHandlers) {
            TriggerHandler existing = handlersByType.putIfAbsent(handler.getSupportedType(), handler);
            if (existing != null) {
                throw new IllegalStateException("Duplicate trigger handler registration for type: " + handler.getSupportedType());
            }
        }
    }

    @Override
    public TriggerHandler get(TriggerType triggerType) {
        TriggerHandler handler = handlersByType.get(triggerType);
        if (handler == null) {
            throw new UnknownTriggerTypeException(triggerType);
        }
        return handler;
    }

    @Override
    public boolean supports(TriggerType triggerType) {
        return handlersByType.containsKey(triggerType);
    }

    public static class UnknownTriggerTypeException extends RuntimeException {
        public UnknownTriggerTypeException(TriggerType triggerType) {
            super("Unknown trigger type: " + triggerType);
        }
    }
}

