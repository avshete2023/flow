package com.flow.connector.registry;

import com.flow.connector.trigger.TriggerHandler;
import com.flow.connector.trigger.TriggerType;

/**
 * Registry for trigger handler lookup by trigger type.
 */
public interface TriggerRegistry {

    TriggerHandler get(TriggerType triggerType);

    boolean supports(TriggerType triggerType);
}

