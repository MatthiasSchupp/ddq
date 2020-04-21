package eu.domaindriven.ddq.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class DdqConfig {

    @SuppressWarnings("squid:S1104")
    public EventConfig event;
}
