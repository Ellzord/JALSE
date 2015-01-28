package jalse.misc;

import java.util.function.Supplier;

public class JALSEExceptions {

    public static final Supplier<RuntimeException> AGENT_ALREADY_ASSOCIATED = () -> new IllegalArgumentException(
	    "Agent is already associated");

    public static final Supplier<RuntimeException> AGENT_LIMIT_REARCHED = () -> new IllegalStateException(
	    "Agent limit has been reached");

    public static final Supplier<RuntimeException> CLUSTER_ALREADY_ASSOCIATED = () -> new IllegalArgumentException(
	    "Cluster is already associated");

    public static final Supplier<RuntimeException> CLUSTER_LIMIT_REARCHED = () -> new IllegalStateException(
	    "Cluster limit has been reached");

    public static final Supplier<RuntimeException> ENGINE_SHUTDOWN = () -> new IllegalStateException(
	    "Engine has already been stopped");

    public static final Supplier<RuntimeException> INVALID_AGENT = () -> new IllegalArgumentException(
	    "Agent is invalid");

    public static final Supplier<RuntimeException> INVALID_ATTRIBUTE_CLASS = () -> new IllegalArgumentException(
	    "Invalid attribute class");

    private JALSEExceptions() {

	throw new UnsupportedOperationException();
    }

    public static void throwRE(final Supplier<RuntimeException> supplier) {

	throw supplier.get();
    }
}
