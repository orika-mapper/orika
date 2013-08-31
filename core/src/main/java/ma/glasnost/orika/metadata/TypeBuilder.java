package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;

/**
 * TypeBuilder is a class used to create a fully populated Type instance
 * based on it's generic declaration.<br><br>
 * 
 * For example, to create a <code>Type&lt;A,&lt;B,C&gt;&gt;</code>, one would use the following:
 * <pre>
 * new TypeBuilder&lt;A,&lt;B,C&gt;&gt;(){}.build();
 * </pre>
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public abstract class TypeBuilder<T> {
    
    private final Class<T> rawType;
    private final java.lang.reflect.Type[] actualTypeArguments;

    /**
     * Constructs a new TypeBuilder instance
     */
    @SuppressWarnings("unchecked")
    public TypeBuilder() {
       
        ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
        java.lang.reflect.Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType)type;
            this.rawType = (Class<T>)parameterizedType.getRawType();
            this.actualTypeArguments = parameterizedType.getActualTypeArguments();
        } else {
            this.rawType = (Class<T>)type;
            this.actualTypeArguments = new java.lang.reflect.Type[0];
        }
    }
    
    /**
     * Construct a new TypeBuilder instance, filling the actual type arguments from the provided
     * types.
     * 
     * @param types
     */
    @SuppressWarnings("unchecked")
    public TypeBuilder(Type<?>...types) {
        ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
        java.lang.reflect.Type type = parameterizedType.getActualTypeArguments()[0];
        /*
         * Assume it's a parameterized type in this case (else, why pass type arguments?)
         */
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException(type + " is not a parameterized type");
        } else {
            parameterizedType = (ParameterizedType)type;
            this.rawType = (Class<T>)parameterizedType.getRawType();
        }
        
        if (types.length != this.rawType.getTypeParameters().length) {
            throw new IllegalArgumentException("wrong number of type arguments; expected " + 
                    this.rawType.getTypeParameters().length + ", but received " + types.length);
        } else {
            this.actualTypeArguments = types.clone();
        }
        
    }
    
    
    /**
     * @return the Type instance built by this builder
     */
    public final Type<T> build() {
    	return TypeFactory.valueOf(rawType, actualTypeArguments);
    }

}
