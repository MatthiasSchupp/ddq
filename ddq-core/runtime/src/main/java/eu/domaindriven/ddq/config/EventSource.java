package eu.domaindriven.ddq.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

import java.util.OptionalLong;

@ConfigGroup
public class EventSource {

    /**
     * Defines the foreign event source.
     *
     * Valid notations are only the hostname, the hostname with protocol, or a full URI.
     */
    @ConfigItem
    public String uri;

    /**
     * Defines the ID from which the events are to be collected.
     *
     * The specified ID is exclusive, which means that only events with a larger ID will be collected.
     * To collect all events the Start ID must be set to 0.
     * To start at the current state, the Start Id must be undefined.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ConfigItem
    public OptionalLong startId;
}
