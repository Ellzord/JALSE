package org.jalse.misc;

import java.util.function.Supplier;

public class JALSEException extends RuntimeException {

    public static final Supplier<JALSEException> AGENT_ALREADY_ASSOCIATED = () -> new JALSEException(
	    "Agent wrapper is already associated");

    public static final Supplier<JALSEException> AGENT_LIMIT_REARCHED = () -> new JALSEException(
	    "Agent limit has been reached");

    public static final Supplier<JALSEException> AGENT_NOT_ASSOCIATED = () -> new JALSEException(
	    "Agent wrapper not associated to Cluster");

    public static final Supplier<JALSEException> CLUSTER_ALREADY_ASSOCIATED = () -> new JALSEException(
	    "Cluster is already associated");

    public static final Supplier<JALSEException> CLUSTER_LIMIT_REARCHED = () -> new JALSEException(
	    "Cluster limit has been reached");

    public static final Supplier<JALSEException> CLUSTER_NOT_ASSOCIATED = () -> new JALSEException(
	    "Cluster is not associated to JALSE");

    public static final Supplier<JALSEException> ENGINE_SHUTDOWN = () -> new JALSEException(
	    "Engine has already been stopped");

    public static final Supplier<JALSEException> INVALID_AGENT_WRAPPER = () -> new JALSEException(
	    "Agent wrapper is invalid");

    public static final Supplier<JALSEException> INVALID_ATTRIBUTE_CLASS = () -> new JALSEException(
	    "Invalid attribute class");

    private static final long serialVersionUID = -7529879607600492980L;

    public JALSEException(final String message) {

	super(message);
    }
}
