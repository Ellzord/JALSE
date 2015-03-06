package jalse.attributes;

import java.util.Objects;

/**
 * This is an existing or final classes {@link Attribute} wrapper. Extending
 * non-attribute wrapper will define the existing class as a attribute type
 * allowing its usage within JALSE. Comparing two non-attribute wrappers will
 * compare the objects they are wrapping.<br>
 * <br>
 *
 * An example non-attribute wrapper:
 *
 * <pre>
 * <code>
 * public class LoadPercentage extends NonAttributeWrapper{@code <Float>} {
 * 
 * public LoadPercentage(Float percentage) {
 * 
 * 	super(percentage);
 * }
 * }
 * 
 * LoadPercentage lp; // Previously created attribute.
 * float percentage = lp.unwrap();
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Existing or final class to wrap.
 *
 * @see Object#equals(Object)
 * @see Object#hashCode()
 */
public abstract class NonAttributeWrapper<T> implements Attribute {

    private final T obj;

    /**
     * Creates a new instance of non-attribute wrapper with the specified
     * object.
     *
     * @param obj
     *            Object to wrap.
     * @throws NullPointerException
     *             If the supplied object is null.
     */
    protected NonAttributeWrapper(final T obj) {

	this.obj = Objects.requireNonNull(obj);
    }

    @Override
    public boolean equals(final Object obj) {

	return this == obj || obj instanceof NonAttributeWrapper<?>
		&& this.obj.equals(((NonAttributeWrapper<?>) obj).obj);
    }

    @Override
    public int hashCode() {

	return Objects.hash(obj);
    }

    /**
     * Gets the value contained in the wrapper.
     *
     * @return Unwraps the object.
     */
    public final T unwrap() {

	return obj;
    }
}
