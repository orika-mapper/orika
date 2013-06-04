package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ma.glasnost.orika.impl.util.ClassUtil;

/**
 * Type is an implementation of ParameterizedType which may be
 * used in various mapping methods where a Class instance would normally
 * be used, in order to provide more specific details as to the actual types
 * represented by the generic template parameters in a given class.<br><br>
 * 
 * Such details are not normally available at runtime using a Class instance
 * due to type-erasure.<br><br>
 * 
 * Type essentially provides a runtime token to represent a ParameterizedType
 * with fully-resolve actual type arguments; it will contain 
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public final class Type<T> implements ParameterizedType, Comparable<Type<?>> {
    
    private static final AtomicInteger nextUniqueIndex = new AtomicInteger();
    
    private final Class<T> rawType;
    private final Type<?>[] actualTypeArguments;
    private final boolean isParameterized;
    private Map<String, Type<?>> typesByVariable;
    private volatile Type<?> superType;
    private volatile Type<?>[] interfaces;
    private Type<?> componentType;
    private final TypeKey key;
    private final int uniqueIndex;

    /**
     * @param rawType
     * @param actualTypeArguments
     */
    @SuppressWarnings("unchecked")
    Type(TypeKey key, Class<?> rawType, Map<String, Type<?>> typesByVariable, Type<?>... actualTypeArguments) {
        this.key = key;
        this.rawType = (Class<T>)rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.typesByVariable = typesByVariable;
        this.isParameterized = rawType.getTypeParameters().length > 0;
        this.uniqueIndex = nextUniqueIndex.getAndIncrement();
    }
    
    /**
     * @return true if the given type is parameterized by nested types
     */
    public boolean isParameterized() {
        return isParameterized;
    }
    
    private Type<?> resolveGenericAncestor(java.lang.reflect.Type ancestor) {
    	Type<?> resolvedType = null;
		if (ancestor instanceof ParameterizedType) {
			resolvedType = TypeFactory.resolveValueOf((ParameterizedType)ancestor, this);
		} else if (ancestor instanceof Class) {
			resolvedType = TypeFactory.valueOf((Class<?>)ancestor);
		} else if (ancestor == null){
		    resolvedType = TypeFactory.TYPE_OF_OBJECT;
		} else {
			throw new IllegalStateException("super-type of " + this.toString() + 
					" is neither Class, nor ParameterizedType, but " + ancestor);
		}
		return resolvedType;
    }
    
    /**
     * @return the unique index of this type
     */
    public int getUniqueIndex() {
        return uniqueIndex;
    }
    
    /**
     * Get the nested Type of the specified index.
     * 
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
	public <X> Type<X> getNestedType(int index) {
    	return (Type<X>)((index > -1 && actualTypeArguments.length > index) ? actualTypeArguments[index] : null);
    }
    
    /**
     * @return the direct super-type of this type, with type arguments resolved with 
     * respect to the actual type arguments of this type.
     * 
     */
    public Type<?> getSuperType() {
    	if (this.superType == null) {
    		synchronized(this) {
	    		if (this.superType == null) {
	    			this.superType = resolveGenericAncestor(rawType.getGenericSuperclass());
	    		}
    		}
    	}
        return this.superType;
    }
    
    /**
     * @return the interfaces implemented by this type, with type arguments resolved with
     * respect to the actual type arguments of this type.
     */
    public Type<?>[] getInterfaces() {
    	if (this.interfaces == null) {
    		synchronized(this) {
	    		if (this.interfaces == null) {
	    		    Type<?>[] interfaces = new Type<?>[rawType.getGenericInterfaces().length];
		    		int i=0;
		    		for (java.lang.reflect.Type interfaceType: rawType.getGenericInterfaces()) {
		    			interfaces[i++] = resolveGenericAncestor(interfaceType);
		    		}
		    		this.interfaces = interfaces;
	    		}
    		}
    	}
    	return interfaces;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
     */
    public java.lang.reflect.Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    public Map<String, Type<?>> getTypesByVariable() {
    	return Collections.unmodifiableMap(typesByVariable);
    }
    
    public java.lang.reflect.Type getTypeByVariable(TypeVariable<?> typeVariable) {
        if (isParameterized) {
            return typesByVariable.get(typeVariable.getName());
        } else {
            return null;
        }
    }
    
    public Class<T> getRawType() {
        return rawType;
    }
    
    public Type<?> getComponentType() {
        if (componentType == null) {
            if (rawType.isArray()) {
            	componentType = TypeFactory.valueOf(rawType.getComponentType());
            } else if (isParameterized){
            	componentType = this.getNestedType(0);
            }
        }
        return componentType;
    }
    
    public java.lang.reflect.Type getOwnerType() {
    	throw new UnsupportedOperationException();
    }
    
    public String getSimpleName() {
        return this.rawType.getSimpleName();
    }
    
    public String getName() {
        return this.rawType.getName();
    }
    
    public String getCanonicalName() {
        return this.rawType.getCanonicalName();
    }
    
    
    
    /**
     * Test whether this type is assignable from the other type.
     * 
     * @param other
     * @return
     */
    public boolean isAssignableFrom(Type<?> other) {
        if (other==null) {
            return false;
        }
        if (!this.getRawType().isAssignableFrom(other.getRawType())) {
            return false;
        }
        if (!this.isParameterized && other.isParameterized) {
            return true;
        } else if (this.rawType.equals(Enum.class) && other.isEnum()){
            return true;
        } else {
        
            if (this.getActualTypeArguments().length!=other.getActualTypeArguments().length) {
                return false;
            }
            java.lang.reflect.Type[] thisTypes = this.getActualTypeArguments();
            java.lang.reflect.Type[] thatTypes = other.getActualTypeArguments();
            for (int i=0, total=thisTypes.length; i < total; ++i ) {
                Type<?> thisType = (Type<?>)thisTypes[i];
                Type<?> thatType = (Type<?>)thatTypes[i];
                // Note: this may be less strict than the rules for compile-time
                // assignability of generic types, but we're only interested in
                // actual runtime types
            	if (!thisType.isAssignableFrom(thatType)) {
            		return false;
            	}   
            }
            return true;
        }
    }
    
    /**
     * Test whether this type is assignable from the other Class;
     * returns true if this type is not parameterized and
     * the raw type is assignable.
     * 
     * @param other
     * @return
     */
    public boolean isAssignableFrom(Class<?> other) {
    	if (other==null) {
            return false;
        }
        if (this.isParameterized()) {
            return false;
        }
        return this.getRawType().isAssignableFrom(other);
    }
    
    public boolean isEnum() {
    	return getRawType().isEnum() || Enum.class.equals(getRawType());
    }
   
    public boolean isArray() {
    	return getRawType().isArray();
    }
    
    public boolean isCollection() {
    	return Collection.class.isAssignableFrom(getRawType());
    }
    
    public boolean isList() {
        return List.class.isAssignableFrom(getRawType());
    }
    
    public boolean isMap() {
    	return Map.class.isAssignableFrom(getRawType());
    }
    
    /**
     * @return true if this type is a Map, Collection or Array
     */
    public boolean isMultiOccurrence() {
        return isMap() || isCollection() || isArray();
    }
    
    public boolean isString() {
        return String.class.isAssignableFrom(getRawType());
    }
    
    public boolean isPrimitive() {
    	return getRawType().isPrimitive();
    }
    
    public boolean isPrimitiveWrapper() {
    	return ClassUtil.isPrimitiveWrapper(getRawType());
    }
    
    public boolean isWrapperFor(Type<?> primitive) {
        return primitive != null && isPrimitiveWrapper() && ClassUtil.getPrimitiveType(this.rawType).equals(primitive.getRawType());
    }
    
    public boolean isPrimitiveFor(Type<?> wrapper) {
        return wrapper != null && isPrimitive() && ClassUtil.getPrimitiveType(wrapper.rawType).equals(getRawType());
    }
    
    public Type<?> getWrapperType() {
        if (!rawType.isPrimitive()) {
            throw new IllegalStateException(rawType + " is not primitive");
        }
        return TypeFactory.valueOf(ClassUtil.getWrapperType(rawType));
    }
    
    /**
     * Finds a class or interface which is an ancestor of this type
     * 
     * @param ancestor
     * @return
     */
    public Type<?> findAncestor(Type<?> ancestor) {
        return findAncestor(ancestor.getRawType());
    }
    
    /**
     * Finds a class or interface which is an ancestor of this type
     * 
     * @param ancestor
     * @return
     */
    public Type<?> findAncestor(Class<?> ancestor) {
        if (ancestor.isInterface()) {
            return findInterface(ancestor);
        } else {
            if (this.getRawType().equals(ancestor)) {
                return this;
            } else if (!TypeFactory.TYPE_OF_OBJECT.equals(this)) {
                return getSuperType().findAncestor(ancestor);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Locates a particular interface within the type's object hierarchy
     * 
     * @param type
     * @param theInterface
     * @return
     */
    private Type<?> findInterface(Class<?> theInterface) {
       
        Type<?> theInterfaceType = null;
        LinkedList<Type<?>> types = new LinkedList<Type<?>>();
        types.add(this);
        while (theInterfaceType == null && !types.isEmpty()) {
            
            Type<?> currentType = types.removeFirst();
            if (theInterface.equals(currentType.getRawType())) {
                theInterfaceType = currentType;
            } else if (!currentType.equals(TypeFactory.TYPE_OF_OBJECT)){
                types.addAll(Arrays.asList(currentType.getInterfaces()));
                types.add(currentType.getSuperType());
            }
        }
        return theInterfaceType;
    }
    
    public Type<?> findInterface(Type<?> theInterface) {
        
        return findInterface(theInterface.rawType);
    }
    
    public Type<?> getPrimitiveType() {
        if (!ClassUtil.isPrimitiveWrapper(rawType)) {
            throw new IllegalStateException(rawType + " is not a primitive wrapper");
        }
        return TypeFactory.valueOf(ClassUtil.getPrimitiveType(rawType));
    }
    
    public boolean isConvertibleFromString() {
    	return ClassUtil.isConvertibleFromString(getRawType());
    }
    
    public String toString() {
    	StringBuilder stringValue = new StringBuilder();
    	if (rawType.isAnonymousClass()) {
    		rawType.getName();
    	} else {	
    		stringValue.append(rawType.getSimpleName());
    	}
    	if (actualTypeArguments.length > 0) {
    		stringValue.append("<");
    		for (java.lang.reflect.Type arg: actualTypeArguments) {
    			stringValue.append(""+arg + ", ");
    		}
    		stringValue.setLength(stringValue.length()-2);
    		stringValue.append(">");
    	}
    	
    	return stringValue.toString();
    }
    
    public String toFullyQualifiedString() {
        StringBuilder stringValue = new StringBuilder();
        stringValue.append(rawType.getCanonicalName());
        if (actualTypeArguments.length > 0) {
            stringValue.append("<");
            for (java.lang.reflect.Type arg: actualTypeArguments) {
                stringValue.append(""+arg + ", ");
            }
            stringValue.setLength(stringValue.length()-2);
            stringValue.append(">");
        }
        
        return stringValue.toString();
    }
    
    @Override
    public int hashCode() {
        //return hashCode;
        // TODO: try guaranteed unique integer index
        return uniqueIndex;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Type<?> other = (Type<?>) obj;
        
        return this.key.equals(other.key);
    }

    
    public int compareTo(Type<?> other) {
        if (this.equals(other)) {
            return 0;
        }
    	String thisChain = buildClassInheritanceChain(this).toString();
    	String otherChain = buildClassInheritanceChain(other).toString();
    	return thisChain.compareTo(otherChain);
    }

	private StringBuilder buildClassInheritanceChain(Type<?> type) {
		if (type.equals(TypeFactory.TYPE_OF_OBJECT))
			return new StringBuilder("/java.lang.Object");
		return buildClassInheritanceChain(type.getSuperType()).append('/')
				.append(type.getName());
	}
}
