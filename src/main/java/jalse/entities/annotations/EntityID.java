package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.functions.Functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * An {@link Entity} type annotation to represent a {@link UUID}. <br>
 * <br>
 * See {@link UUID#UUID(long, long)}, {@link UUID#fromString(String)} and {@link UUID#randomUUID()}.
 *
 * @author Elliot
 *
 * @see Functions#toIDSupplier(EntityID)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(EntityIDs.class)
public @interface EntityID {

    /**
     * Default last significant bits ({@code 0L}).
     */
    public static final long DEFAULT_LEAST_SIG_BITS = 0L;

    /**
     * Default most significant bits ({@code 0L}).
     */
    public static final long DEFAULT_MOST_SIG_BITS = 0L;

    /**
     * Default name ({@code ""}).
     */
    public static final String DEFAULT_NAME = "";

    /**
     * Default random ({@code false}).
     */
    public static final boolean DEFAULT_RANDOM = false;

    /**
     * The least significant bits.
     *
     * @return Least significant bits.
     *
     * @see EntityID#DEFAULT_LEAST_SIG_BITS
     */
    long leastSigBits() default DEFAULT_LEAST_SIG_BITS;

    /**
     * The most significant bits.
     *
     * @return most significant bits.
     *
     * @see EntityID#DEFAULT_MOST_SIG_BITS
     */
    long mostSigBits() default DEFAULT_MOST_SIG_BITS;

    /**
     * String representation of the ID.
     *
     * @return String representation.
     *
     * @see EntityID#DEFAULT_NAME
     */
    String name() default DEFAULT_NAME;

    /**
     * Whether the ID should be random.
     *
     * @return Random ID.
     *
     * @see EntityID#DEFAULT_RANDOM
     */
    boolean random() default DEFAULT_RANDOM;
}
