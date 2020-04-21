package eu.domaindriven.ddq.notification;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class NotificationLogId {

    private final long low;
    private final long high;

    public NotificationLogId(long low, long high) {
        this.low = low;
        this.high = high;
    }

    public NotificationLogId(String notificationLogId) {
        String[] textIds = notificationLogId.split(",");
        this.low = Long.parseLong(textIds[0]);
        this.high = Long.parseLong(textIds[1]);
    }

    public String encoded() {
        return "" + low() + ',' + high();
    }

    public long low() {
        return low;
    }

    public long high() {
        return high;
    }

    public int size() {
        return (int) (high() - low() + 1);
    }

    public Optional<NotificationLogId> next(int logSize) {
        long nextLow = high() + 1;
        long nextHigh = nextLow + logSize - 1;

        NotificationLogId next = new NotificationLogId(nextLow, nextHigh);
        return this.equals(next)
                ? Optional.empty()
                : Optional.of(next);
    }

    public Optional<NotificationLogId> previous(int logSize) {
        long previousLow = Math.max(this.low() - logSize, 1);
        long previousHigh = previousLow + logSize - 1;

        NotificationLogId previous = new NotificationLogId(previousLow, previousHigh);
        return this.equals(previous)
                ? Optional.empty()
                : Optional.of(previous);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationLogId that = (NotificationLogId) o;
        return low == that.low &&
                high == that.high;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationLogId.class.getSimpleName() + "[", "]")
                .add("low=" + low)
                .add("high=" + high)
                .toString();
    }
}
