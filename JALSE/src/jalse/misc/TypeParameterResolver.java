package jalse.misc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A parameter resolver for resolving actual type arguments for generic type parameters. The
 * resolver will resolve the actual type argument for a supplied type parameter (this resolved type
 * may itself be a generic type). This class also provides easy means to get type parameter
 * instances and transform Type to Class.<br>
 * <br>
 * NOTE: Lambda classes do not contain sufficient generic type information and do not work with this
 * resolver.<br>
 * <br>
 *
 * An example resolution:
 *
 * <pre>
 * <code>
 * Supplier<?> sup; // Previously initialised Supplier.
 * 
 * TypeParameterResolver resolver = new TypeParameterResolver(Supplier.class, "T");
 * Class<?> resolvedClazz = toClass(resolver.resolve(sup));
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 *
 * @see #getTypeParameter(Class, String)
 * @see #toClass(Type)
 *
 */
public final class TypeParameterResolver {

    private class ResolvedParameterizedType implements ParameterizedType {

	private final Type[] actualTypeArguments;
	private final Type ownerType;
	private final Class<?> rawType;

	public ResolvedParameterizedType(final Class<?> rawType, final Type[] actualTypeArguments, final Type ownerType) {
	    this.rawType = rawType;
	    this.actualTypeArguments = actualTypeArguments;
	    this.ownerType = ownerType;
	}

	@Override
	public Type[] getActualTypeArguments() {
	    return actualTypeArguments;
	}

	@Override
	public Type getOwnerType() {
	    return ownerType;
	}

	@Override
	public Type getRawType() {
	    return rawType;
	}
    }

    /**
     * Gets the named type parameter for a given defining class.
     *
     * @param clazz
     *            Class the parameter is defined.
     * @param name
     *            Type parameter name.
     * @return The type parameter instance.
     */
    public static TypeVariable<? extends Class<?>> getTypeParameter(final Class<?> clazz, final String name) {
	for (final TypeVariable<? extends Class<?>> param : clazz.getTypeParameters()) {
	    if (param.getName().equals(name)) { // Like "T", "S", "U", etc
		return param;
	    }
	}
	return null;
    }

    /**
     * Gets a Class instance for a given type.
     *
     * @param type
     *            Type to transform.
     * @return Transformed type object or null if no transformation could be made.
     */
    public static Class<?> toClass(final Type type) {
	Class<?> result = null;
	if (type instanceof Class) {
	    result = (Class<?>) type;
	} else if (type instanceof ParameterizedType) { // Could be our resolved
	    result = toClass(((ParameterizedType) type).getRawType());
	}
	return result;
    }

    private final Class<?> declaringClazz;
    private final TypeVariable<? extends Class<?>> typeParameter;

    /**
     * Creates a new instance of TypeParameterResolver to resolve the supplied parameter. This will
     * look up the parameter using the class supplied and the parameter name.
     *
     * @param clazz
     *            Defining class.
     * @param name
     *            Type parameter name.
     *
     * @see #getTypeParameter(Class, String)
     */
    public TypeParameterResolver(final Class<?> clazz, final String name) {
	this(getTypeParameter(clazz, name));
    }

    /**
     * Creates a new instance of TypeParameterResolver that will look to resolve the supplied
     * parameter.
     *
     * @param param
     *            Parameter to resolve.
     *
     * @throws NullPointerException
     *             If the parameter is null.
     */
    public TypeParameterResolver(final TypeVariable<? extends Class<?>> param) {
	typeParameter = Objects.requireNonNull(param);
	declaringClazz = param.getGenericDeclaration();
    }

    /**
     * Gets the type parameter this resolver is looking for.
     *
     * @return Type parameter to resolve.
     */
    public TypeVariable<? extends Class<?>> getTypeParameter() {
	return typeParameter;
    }

    /**
     * Resolves the actual type arguments for the supplied type.
     *
     * @param obj
     *            Instance to get actual type arguments for.
     * @return Actual type arguments for the parameter.
     *
     * @throws IllegalArgumentException
     *             If the supplied instance does not have the parameter's defining class in its
     *             class tree.
     */
    public Type resolve(final Object obj) {
	return resolve(obj.getClass());
    }

    /**
     * Resolves the actual type arguments for the supplied type.
     *
     * @param type
     *            Type to get actual type arguments for.
     * @return Actual type arguments for the parameter.
     *
     * @throws IllegalArgumentException
     *             If the supplied type does not have the parameter's defining class in its class
     *             tree.
     */
    public Type resolve(final Type type) {
	final Type superType = resolveRelevantSuperType(type);
	if (superType != null) {
	    int index = 0;
	    for (final TypeVariable<?> param : declaringClazz.getTypeParameters()) {
		if (typeParameter.equals(param)) {
		    return ((ParameterizedType) superType).getActualTypeArguments()[index];
		}
		index++; // Indexes match
	    }
	}
	throw new IllegalArgumentException(String.format("Type does not have %s in its class tree", declaringClazz));
    }

    private Type resolveActualTypeParameter(final Type toMap, final Type type) {
	if (toMap instanceof Class) {
	    return toMap;
	}

	final Map<TypeVariable<?>, Type> paramsToArgs = new HashMap<>();
	Type currentOwner = type;

	while (currentOwner instanceof ParameterizedType) {
	    final ParameterizedType pt = (ParameterizedType) currentOwner;
	    final Type[] args = pt.getActualTypeArguments();
	    int index = 0;
	    for (final TypeVariable<?> param : ((Class<?>) pt.getRawType()).getTypeParameters()) {
		paramsToArgs.put(param, args[index++]); // Map
	    }
	    currentOwner = pt.getOwnerType();
	}

	final ParameterizedType pt = (ParameterizedType) toMap;
	final Type[] args = pt.getActualTypeArguments();
	final Type[] resolvedArgs = new Type[args.length];

	for (int i = 0; i < args.length; i++) {
	    final Type arg = args[i];
	    resolvedArgs[i] = arg instanceof Class ? arg : paramsToArgs.get(arg); // Resolve
	}

	return new ResolvedParameterizedType((Class<?>) pt.getRawType(), resolvedArgs, pt.getOwnerType());
    }

    private Type[] resolveDirectSuperTypes(final Type type) {
	final Class<?> clazz = toClass(type);
	if (clazz.isSynthetic()) {
	    throw new UnsupportedOperationException("Lambda classes do not store full generic type information");
	}

	final Type[] superInterfaces = clazz.getGenericInterfaces();
	final Type superClazz = clazz.getGenericSuperclass();
	final boolean noSuperClazz = superClazz == null || Object.class.equals(superClazz);

	final Type[] result = new Type[superInterfaces.length + (noSuperClazz ? 0 : 1)];
	int index = 0;

	if (!noSuperClazz) {
	    result[index++] = resolveActualTypeParameter(superClazz, type);
	}

	for (final Type superInterface : superInterfaces) {
	    result[index++] = resolveActualTypeParameter(superInterface, type);
	}

	return result;
    }

    private Type resolveRelevantSuperType(final Type type) {
	if (declaringClazz.equals(toClass(type))) {
	    return type;
	}

	for (final Type superType : resolveDirectSuperTypes(type)) {
	    final Type result = resolveRelevantSuperType(superType);
	    if (result != null) { // Resolved!
		return result;
	    }
	}

	return null;
    }
}
