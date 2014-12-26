package jalse.actions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface Scheduler<T> {

    boolean cancel(final UUID action);

    boolean isActive(final UUID action);

    UUID schedule(final Action<T> action);

    UUID schedule(final Action<T> action, final long initialDelay, final long period, final TimeUnit unit);

    UUID schedule(final Action<T> action, final long initialDelay, final TimeUnit unit);
}
