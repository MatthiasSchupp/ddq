package eu.domaindriven.ddq;

import java.util.UUID;

@SuppressWarnings("UtilityClass")
public final class Instance {

    private Instance() {
    }

    public static UUID id() {
        return InstanceIdHolder.ID;
    }

    private static class InstanceIdHolder {

        private static final UUID ID = UUID.randomUUID();
    }
}
