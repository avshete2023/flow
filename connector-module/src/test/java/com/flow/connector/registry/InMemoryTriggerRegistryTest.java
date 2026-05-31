package com.flow.connector.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.trigger.TriggerHandler;
import com.flow.connector.trigger.TriggerType;
import com.flow.connector.trigger.TriggerValidationModel;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryTriggerRegistryTest {

    @Test
    void shouldRegisterHandlersAndLookupByTriggerType() {
        TriggerHandler scheduler = new TestTriggerHandler(TriggerType.SCHEDULER);
        TriggerHandler webhook = new TestTriggerHandler(TriggerType.WEBHOOK);

        InMemoryTriggerRegistry registry = new InMemoryTriggerRegistry(List.of(scheduler, webhook));

        assertThat(registry.supports(TriggerType.SCHEDULER)).isTrue();
        assertThat(registry.supports(TriggerType.WEBHOOK)).isTrue();
        assertThat(registry.supports(TriggerType.EVENT)).isFalse();
        assertThat(registry.get(TriggerType.SCHEDULER)).isSameAs(scheduler);
        assertThat(registry.get(TriggerType.WEBHOOK)).isSameAs(webhook);
    }

    @Test
    void shouldThrowForUnknownTriggerType() {
        InMemoryTriggerRegistry registry = new InMemoryTriggerRegistry(List.of(new TestTriggerHandler(TriggerType.SCHEDULER)));

        assertThatThrownBy(() -> registry.get(TriggerType.EVENT))
                .isInstanceOf(InMemoryTriggerRegistry.UnknownTriggerTypeException.class)
                .hasMessageContaining("Unknown trigger type: EVENT");
    }

    @Test
    void shouldRejectDuplicateTriggerTypeRegistration() {
        TriggerHandler first = new TestTriggerHandler(TriggerType.WEBHOOK);
        TriggerHandler duplicate = new TestTriggerHandler(TriggerType.WEBHOOK);

        assertThatThrownBy(() -> new InMemoryTriggerRegistry(List.of(first, duplicate)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate trigger handler registration");
    }

    private static final class TestTriggerHandler implements TriggerHandler {

        private final TriggerType triggerType;

        private TestTriggerHandler(TriggerType triggerType) {
            this.triggerType = triggerType;
        }

        @Override
        public TriggerType getSupportedType() {
            return triggerType;
        }

        @Override
        public TriggerValidationModel validateConfiguration(JsonNode configuration) {
            if (configuration == null || !configuration.isObject()) {
                return TriggerValidationModel.failure(List.of("Configuration must be object"));
            }
            return TriggerValidationModel.success();
        }
    }
}


