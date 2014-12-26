package jalse.attributes;

import java.util.Objects;
import java.util.Optional;

public abstract class NonAttributeWrapper<T> implements Attribute {

    public static <S> S forceUnwrap(final Optional<? extends NonAttributeWrapper<S>> attr) {

	return attr.get().unwrap();
    }

    private final T obj;

    public NonAttributeWrapper(final T obj) {

	this.obj = Objects.requireNonNull(obj);
    }

    @Override
    public boolean equals(final Object obj) {

	return this == obj || obj instanceof NonAttributeWrapper<?>
	&& Objects.equals(this.obj, ((NonAttributeWrapper<?>) obj).obj);
    }

    @Override
    public int hashCode() {

	return Objects.hash(obj);
    }

    public final T unwrap() {

	return obj;
    }
}
