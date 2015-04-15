package jalse;

import static jalse.misc.Identifiable.DUMMY_ID;
import jalse.actions.ActionEngine;
import jalse.actions.ForkJoinActionEngine;
import jalse.actions.ManualActionEngine;
import jalse.actions.ThreadPoolActionEngine;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entity;
import jalse.misc.Identifiable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

/**
 * A {@link JALSE} instance builder where each method in this builder can be chained. This builder
 * constructs a JALSE instance using the supplied parameters. It does this by using the appropriate
 * {@link ActionEngine} implementation along with a {@link DefaultEntityFactory}. JALSE can still be
 * created without this builder.<br>
 * <br>
 * By default this builder uses {@link #setCommonPoolEngine()}, {@link #setRandomID()} and
 * {@link #setNoEntityLimit()}. <br>
 * <br>
 * If {@link Entity} must be transfered externally (between two JALSE instances) then unique IDs
 * should be set ({@link UUID#randomUUID()} is used by default).
 *
 * @author Elliot Ford
 *
 *
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 * @see ManualActionEngine
 *
 */
public final class JALSEBuilder {

    /**
     * Creates a common pool JALSE instance (with a random ID and no entity limit).
     *
     * @return Default parallelism JALSE instance.
     *
     * @see #setRandomID()
     * @see #setCommonPoolEngine()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildCommonPoolJALSE() {
	return newBuilder().build();
    }

    /**
     * Builds a manually ticked JALSE instance (with a random ID and no entity limit).
     *
     * @return Manual tick JALSE.
     *
     * @see #setRandomID()
     * @see #setManual()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildManualJALSE() {
	return newBuilder().setManual().build();
    }

    /**
     * Builds a single threaded JALSE instance (with a random ID and no entity limit).
     *
     * @return Single threaded JALSE instance.
     *
     * @see #setRandomID()
     * @see #setSingleThread()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildSingleThreadedJALSE() {
	return newBuilder().setParallelism(1).build();
    }

    /**
     * Creates a new builder with defaults.
     *
     * @return New builder.
     */
    public static JALSEBuilder newBuilder() {
	return new JALSEBuilder();
    }

    private static int requireAboveOne(final int value) {
	if (value < 1) {
	    throw new IllegalArgumentException();
	}
	return value;
    }

    private UUID id;
    private int parallelism;
    private int totalEntityLimit;
    private boolean commonPool;

    private boolean manual;

    private JALSEBuilder() {
	setRandomID();
	setCommonPoolEngine();
	setNoEntityLimit();
    }

    /**
     * Builds an instance of JALSE with the supplied parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE build() {
	final ActionEngine engine;

	if (commonPool) {
	    engine = ForkJoinActionEngine.commonPoolEngine();
	} else if (manual) {
	    engine = new ManualActionEngine();
	} else if (parallelism == 1) {
	    engine = new ThreadPoolActionEngine(1);
	} else {
	    engine = new ForkJoinActionEngine(parallelism);
	}

	return new JALSE(id, engine, new DefaultEntityFactory(totalEntityLimit));
    }

    /**
     * Users the common engine based on the common pool.
     *
     * @return This builder.
     *
     * @see ForkJoinPool#commonPool()
     * @see ForkJoinActionEngine#commonPoolEngine()
     */
    public JALSEBuilder setCommonPoolEngine() {
	commonPool = true;
	manual = false;
	parallelism = 0;
	return this;
    }

    /**
     * Sets the unique ID for JALSE to a dummy ID.
     *
     * @return This builder.
     *
     * @see Identifiable#DUMMY_ID
     */
    public JALSEBuilder setDummyID() {
	return setID(DUMMY_ID);
    }

    /**
     * Sets the unique ID for JALSE instance.
     *
     * @param id
     *            ID of JALSE.
     * @return This builder.
     */
    public JALSEBuilder setID(final UUID id) {
	this.id = Objects.requireNonNull(id);
	return this;
    }

    /**
     * Sets the engine to be a manual tick engine.
     *
     * @return This builder.
     */
    public JALSEBuilder setManual() {
	manual = true;
	commonPool = false;
	parallelism = 0;
	return this;
    }

    /**
     * Sets there to be no entity limit.
     *
     * @return This builder.
     *
     * @see Integer#MAX_VALUE
     */
    public JALSEBuilder setNoEntityLimit() {
	return setTotalEntityLimit(Integer.MAX_VALUE);
    }

    /**
     * Sets the parallelism to be utilised by the engine.
     *
     * @param parallelism
     *            Thread parallelism.
     * @return This builder.
     */
    public JALSEBuilder setParallelism(final int parallelism) {
	this.parallelism = requireAboveOne(parallelism);
	commonPool = false;
	manual = false;
	return this;
    }

    /**
     * Sets the parallelism to the available processors.
     *
     * @return This builder.
     *
     * @see Runtime#availableProcessors()
     */
    public JALSEBuilder setParallelismToProcessors() {
	return setParallelism(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Sets the ID to a random one.
     *
     * @return This builder.
     *
     * @see UUID#randomUUID()
     */
    public JALSEBuilder setRandomID() {
	return setID(UUID.randomUUID());
    }

    /**
     * Sets to use a single thread.
     *
     * @return This builder.
     */
    public JALSEBuilder setSingleThread() {
	return setParallelism(1);
    }

    /**
     * Sets the total entity limit parameter.
     *
     * @param totalEntityLimit
     *            Maximum entity limited.
     * @return This builder.
     */
    public JALSEBuilder setTotalEntityLimit(final int totalEntityLimit) {
	this.totalEntityLimit = requireAboveOne(totalEntityLimit);
	return this;
    }
}
