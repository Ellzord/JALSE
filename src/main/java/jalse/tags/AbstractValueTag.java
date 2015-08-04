package jalse.tags;

import java.util.Objects;

/**
 * A simple implementation for single value {@link Tag}s. <br>
 * <br>
 * For equality this class with check the class of the instance (as well as the value). This is
 * consistent to how {@link Tag}s are stored uniquely.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Tag type.
 */
public abstract class AbstractValueTag<T> implements Tag {

    private final T value;

    /**
     * Creates a new value tag.
     *
     * @param value
     *            Value to set.
     */
    public AbstractValueTag(final T value) {
	this.value = Objects.requireNonNull(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object obj) {
	return obj == this
		|| obj != null && getClass().equals(obj.getClass()) && value.equals(((AbstractValueTag<T>) obj).value);
    }

    /**
     * Gets the value.
     *
     * @return Value.
     */
    public T getValue() {
	return value;
    }

    @Override
    public final int hashCode() {
	return 31 * 1 + value.hashCode();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [" + value + "]";
    }
}
