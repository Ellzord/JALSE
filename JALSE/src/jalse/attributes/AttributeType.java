package jalse.attributes;

import static jalse.misc.JALSEExceptions.INVALID_ATTRTYPE_SUBTYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.listeners.AttributeListener;
import jalse.misc.JALSEExceptions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

/**
 * AttributeTypes and their values can be considered the core data of the JALSE model. AttributeType
 * defines what type the data must be. {@link AttributeContainer} is an attribute container where
 * attributes can be stored and {@link AttributeListener} can be set to trigger on value updates.<br>
 * <br>
 * For simple value types (no type arguments) {@link Attributes#newTypeOf(Class)} can be used:
 *
 * <pre>
 * <code>
 * AttributeType{@code <String>} stringType = Attributes.newTypeOf(String.class);
 * </code>
 * </pre>
 *
 * For more complex types an anonymous (or declared) subclasses should be created supplying the
 * intended value type as the type argument. This is needed so full generic type information can be
 * retrieved then raw and parameterised types can be differentiated:
 *
 * <pre>
 * <code>
 * AttributeType{@code <Collection<String>>} colStringType = new AttributeType{@code <Collection<String>>}(){};
 * </code>
 * </pre>
 *
 * When using reflection or where type erasure is not possible
 * {@link Attributes#newUnknownType(Type)} can be used. This will be unique to the type not to
 * {@code Object.class}.<br>
 * <br>
 * AttributeTypes are meant to be easily distinguishable but for full generic information a
 * Anonymous class is needed. To ensure this behaviour and so that AttributeType can be used as a
 * unique key {@link #equals(Object)}, {@link #hashCode()} and {@link #getValueType()} are all
 * {@code final}.
 *
 * @author Elliot Ford
 * @param <T>
 *            Value type.
 *
 * @see AttributeContainer
 * @see AttributeListener
 * @see AttributeSet
 * @see Attributes
 *
 */
public abstract class AttributeType<T> {

    /**
     * Attribute value type.
     */
    protected final Type valueType;

    /**
     * Creates a new instance of Attribute type (using generic type information).
     *
     * @see JALSEExceptions#INVALID_ATTRTYPE_SUBTYPE
     */
    public AttributeType() {
	/*
	 * Get direct superclass of AttributeType.
	 */
	Class<?> superType = getClass();
	while (!superType.getSuperclass().equals(AttributeType.class)) {
	    superType = superType.getSuperclass();
	}
	/*
	 * Get generic parameter type.
	 */
	final ParameterizedType genericSuperType = (ParameterizedType) getClass().getGenericSuperclass();
	final Type typeArg = genericSuperType.getActualTypeArguments()[0]; // T

	if (typeArg instanceof TypeVariable<?>) { // Could not get generic type argument.
	    throwRE(INVALID_ATTRTYPE_SUBTYPE);
	}

	this.valueType = typeArg;
    }

    AttributeType(final Type valueType) {
	this.valueType = Objects.requireNonNull(valueType);
    }

    @Override
    public final boolean equals(final Object obj) {
	return obj == this || obj instanceof AttributeType<?> && valueType.equals(((AttributeType<?>) obj).valueType);
    }

    /**
     * Gets attribute value type.
     *
     * @return Value type.
     */
    public final Type getValueType() {
	return valueType;
    }

    @Override
    public final int hashCode() {
	return 31 * (valueType == null ? 0 : valueType.hashCode());
    }

    @Override
    public String toString() {
	return "AttributeType [valueType=" + valueType.getTypeName() + "]";
    }
}