package org.jalse.attributes;

import java.util.Objects;

public abstract class NonAttributeWrapper<T> implements Attribute {

    private final T obj;

    public NonAttributeWrapper(final T obj) {

	this.obj = Objects.requireNonNull(obj);
    }

    @Override
    public boolean equals(final Object obj) {

	return this == obj || obj instanceof NonAttributeWrapper<?>
		&& Objects.equals(((NonAttributeWrapper<?>) obj).obj, this.obj);
    }

    @Override
    public int hashCode() {

	return Objects.hash(obj);
    }

    public final T unwrap() {

	return obj;
    }
}
