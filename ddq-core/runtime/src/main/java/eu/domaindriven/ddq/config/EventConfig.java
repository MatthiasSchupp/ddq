package eu.domaindriven.ddq.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

import java.time.Duration;
import java.util.Map;

@ConfigGroup
public class EventConfig {

    /**
     * Defines the duration after the processing of an event is regarded as failed and this can be processed again by
     * another instance.
     *
     */
    @ConfigItem(defaultValue = "PT1M")
    public Duration processingThreshold;

    /**
     * Defines the notification endpoints of other services from which this service should collect the published events.
     *
     * Valid notations are only the hostname, the hostname with protocol, or a full URI.
     *
     */
    @ConfigItem(name = "event-source")
    public Map<String, EventSource> eventSources;

    /**
     * Defines the protocol to use when collecting foreign domain events.
     */
    @ConfigItem(defaultValue = "http")
    public String eventSourceProtocol;

    /**
     * Defines the resource where the foreign domain events can be collected.
     */
    @ConfigItem(defaultValue = "/resources/notifications/events")
    public String eventSourceResource;
}
