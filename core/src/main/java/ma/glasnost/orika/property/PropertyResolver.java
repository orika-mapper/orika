/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.property;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.PropertyNotFoundException;
import ma.glasnost.orika.metadata.ArrayElementProperty;
import ma.glasnost.orika.metadata.ListElementProperty;
import ma.glasnost.orika.metadata.MapKeyProperty;
import ma.glasnost.orika.metadata.NestedElementProperty;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * PropertyResolver defines the core functionality for resolving properties;
 * specific PropertyResolverStrategy implementations should extend from this
 * class, implementing the 'collectProperties' method.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public abstract class PropertyResolver implements PropertyResolverStrategy {
    
    /**
     * The prefix used to begin a nested element property
     */
    public static final String ELEMENT_PROPERT_PREFIX = "{";
    /**
     * The suffix used to complete a nested element property
     */
    public static final String ELEMENT_PROPERT_SUFFIX = "}";
    
    private final boolean includePublicFields;
    
    private final Map<java.lang.reflect.Type, Map<String, Property>> propertiesCache = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    private final Map<java.lang.reflect.Type, Map<String, Property>> inlinePropertiesCache = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    
    /**
     * Creates a new PropertyResolver instance
     * 
     * @param includePublicFields
     *            whether public fields should be included as properties
     */
    public PropertyResolver(boolean includePublicFields) {
        this.includePublicFields = includePublicFields;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.property.PropertyResolverStrategy#getProperties(java
     * .lang.reflect.Type)
     */
    public Map<String, Property> getProperties(java.lang.reflect.Type theType) {
        
        Map<String, Property> properties = propertiesCache.get(theType);
        if (properties == null) {
            synchronized (theType) {
                properties = propertiesCache.get(theType);
                if (properties == null) {
                    
                    properties = new LinkedHashMap<String, Property>();
                    Type<?> referenceType;
                    
                    if (theType instanceof Type) {
                        referenceType = (Type<?>) theType;
                    } else if (theType instanceof Class) {
                        referenceType = TypeFactory.valueOf((Class<?>) theType);
                    } else {
                        throw new IllegalArgumentException("type " + theType + " not supported.");
                    }
                    
                    /*
                     * We process the properties for a type by starting with the
                     * type itself, and then continue by processing the classes
                     * and interfaces in the type's hierarchy. Property
                     * definitions from an ancestor should not override those
                     * already defined.
                     */
                    LinkedList<Class<? extends Object>> types = new LinkedList<Class<? extends Object>>();
                    types.addFirst((Class<? extends Object>) referenceType.getRawType());
                    while (!types.isEmpty()) {
                        Class<? extends Object> type = types.removeFirst();
                        
                        collectProperties(type, referenceType, properties);
                        
                        if (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
                            types.add(type.getSuperclass());
                        }
                        
                        List<? extends Class<? extends Object>> interfaces = Arrays.<Class<? extends Object>> asList(type.getInterfaces());
                        types.addAll(interfaces);
                    }
                    
                    if (includePublicFields) {
                        /*
                         * Call this outside of the loop because the fields
                         * returned are already inclusive of ancestors.
                         */
                        collectPublicFieldProperties(referenceType, properties);
                    }
                    
                    propertiesCache.put(theType, Collections.unmodifiableMap(properties));
                }
            }
        }
        return properties;
    }
    
    /**
     * Attempt to resolve the generic type, using refereceType to resolve
     * TypeVariables
     * 
     * @param genericType
     *            the type to resolve
     * @param referenceType
     *            the reference type to use for lookup of type variables
     * @return
     */
    private Type<?> resolveGenericType(java.lang.reflect.Type genericType, Type<?> referenceType) {
        Type<?> resolvedType = null;
        Type<?> reference = referenceType;
        do {
            if (genericType instanceof TypeVariable && reference.isParameterized()) {
                java.lang.reflect.Type t = reference.getTypeByVariable((TypeVariable<?>) genericType);
                if (t != null) {
                    resolvedType = TypeFactory.valueOf(t);
                }
            } else if (genericType instanceof ParameterizedType) {
                if (reference.isParameterized()) {
                    resolvedType = TypeFactory.resolveValueOf((ParameterizedType) genericType, reference);
                } else {
                    resolvedType = TypeFactory.valueOf((ParameterizedType) genericType);
                }
            }
            reference = reference.getSuperType();
        } while (resolvedType == null && reference != TypeFactory.TYPE_OF_OBJECT);
        return resolvedType;
    }
    
    /**
     * Convert the first character of the provided string to uppercase.
     * 
     * @param string
     * @return the String with the first character converter to uppercase.
     */
    protected String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    /**
     * Resolves the raw property type from a property descriptor; if a read
     * method is available, use it to refine the type. The results of
     * pd.getPropertyType() are sometimes inconsistent across platforms.
     * 
     * @param pd
     * @return
     */
    private Class<?> resolveRawPropertyType(Class<?> rawType, Method readMethod) {
        try {
            return (readMethod == null ? rawType : readMethod.getDeclaringClass()
                    .getDeclaredMethod(readMethod.getName(), new Class[0])
                    .getReturnType());
        } catch (Exception e) {
            return rawType;
        }
    }
    
    /**
     * Processes a property, adding it to the map of properties for the owning
     * type being processed
     * 
     * @param propertyName
     *            the name of the property
     * @param propertyType
     *            the Class of the property
     * @param readMethod
     *            the read method for the property
     * @param writeMethod
     *            the write method for the property
     * @param owningType
     *            the owning type for which the properties are being resolved
     * @param referenceType
     *            a reference type to be used for resolving generic parameters
     * @param properties
     */
    protected Property processProperty(String propertyName, Class<?> propertyType, Method readMethod, Method writeMethod, Class<?> owningType,
            Type<?> referenceType, Map<String, Property> properties) {
        final Property.Builder builder = new Property.Builder();
        
        Property property = null;
        
        builder.expression(propertyName);
        builder.name(propertyName);
        
        if (readMethod != null) {
            builder.getter(readMethod.getName() + "()");
        }
        if (writeMethod != null) {
            builder.setter(writeMethod.getName() + "(%s)");
        }
        
        if (readMethod != null || writeMethod != null) {
            
            builder.type(resolvePropertyType(readMethod, propertyType, owningType, referenceType));
            property = builder.build(this);
            
            Property existing = properties.get(propertyName);
            if (existing == null) {
                properties.put(propertyName, property);
            } else if (existing.getType().isAssignableFrom(property.getType()) /*&& !existing.getType().equals(property.getType())*/) {
                /*
                 * The type has been refined by the generic information in a
                 * super-type
                 */
            	property = builder.merge(existing).build(this);
                properties.put(propertyName, property);
            }
        }
        return property;
    }
    
    /**
     * Tests whether the specified class has type parameters either on
     * itself or on it's super-class or declared interfaces
     * 
     * @param type
     * @return
     */
    protected boolean hasTypeParameters(Class<?> type) {
    	
    	boolean hasTypeParams = false;
    	if (type.getTypeParameters().length > 0) {
    		hasTypeParams = true;
    	} else {
    		if (type.getGenericSuperclass() instanceof ParameterizedType) {
    			hasTypeParams = true;
    		} else {
    			for (java.lang.reflect.Type anInterface: type.getGenericInterfaces()) {
    				if (anInterface instanceof ParameterizedType) {
    					hasTypeParams = true;
    					break;
    				}
    			}
    		}
    	}
    	return hasTypeParams;
    	
    }
    
    /**
     * Resolves the type of a property from the provided input factors.
     * 
     * @param readMethod
     * @param rawType
     * @param owningType
     * @param referenceType
     * @return the resolved Type of the property
     */
    public Type<?> resolvePropertyType(Method readMethod, Class<?> rawType, Class<?> owningType, Type<?> referenceType) {
        
        rawType = resolveRawPropertyType(rawType, readMethod);
        
        Type<?> resolvedGenericType = null;
        if (referenceType.isParameterized() || hasTypeParameters(owningType) || hasTypeParameters(rawType)) {
            
            if (readMethod != null) {
                try {
                    resolvedGenericType = resolveGenericType(
                            readMethod.getDeclaringClass().getDeclaredMethod(readMethod.getName(), new Class[0]).getGenericReturnType(),
                            referenceType);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("readMethod does not exist", e);
                }
            }
        }
        
        if (resolvedGenericType == null || resolvedGenericType.isAssignableFrom(rawType)) {
            resolvedGenericType = TypeFactory.valueOf(rawType);
        }
        return resolvedGenericType;
    }
    
    /**
     * Add public non-static fields as properties
     * 
     * @param referenceType
     *            the type for which to collect public field properties
     * @param properties
     *            the collected properties for this type
     */
    protected void collectPublicFieldProperties(Type<?> referenceType, Map<String, Property> properties) {
        
        for (Field f : referenceType.getRawType().getFields()) {
            if (!isStatic(f.getModifiers())) {
                final Property.Builder builder = new Property.Builder();
                builder.expression(f.getName());
                builder.name(f.getName());
                
                Class<?> rawType = f.getType();
                Type<?> genericType = resolveGenericType(f.getGenericType(), referenceType);
                if (genericType != null && !genericType.isAssignableFrom(rawType)) {
                    builder.type(genericType);
                } else {
                    builder.type(TypeFactory.valueOf(rawType));
                }
                
                if (!isFinal(f.getModifiers())) {
                    builder.setter(f.getName() + " = %s");
                }

                Property existing = properties.get(f.getName());
                if (existing == null) {
                    builder.getter(f.getName());
                    properties.put(f.getName(), builder.build(this));
                } else if (existing.getSetter() == null) {
                    builder.merge(existing);
                    properties.put(f.getName(), builder.build(this));
                }
            }
        }
    }
    
    /**
     * Determines whether the provided string is a valid nested property
     * expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return true of the expression represents a nested property
     */
    protected boolean isNestedPropertyExpression(String expression) {
        return expression.replaceAll("\\:\\{" + NON_NESTED_PROPERTY_CHARACTERS + "\\}", "").indexOf('.') != -1;
    }
    
    /**
     * Determines whether the provided string is a valid element property
     * expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return true if the expression represents an element property
     */
    protected boolean isElementPropertyExpression(String expression) {
        return expression.replaceAll("\\:\\{" + DYNAMIC_PROPERTY_CHARACTERS + "\\}", "").indexOf('{') != -1;
    }
    
    
    /**
     * Determines whether the provided string is a valid element property
     * expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return true if the expression represents an element property
     */
    protected boolean isIndividualElementExpression(String expression) {
        String normalized = expression.replaceAll("\\:\\{" + DYNAMIC_PROPERTY_CHARACTERS + "\\}", "");
        return normalized.contains("[") && normalized.endsWith("]");
    }
    
    /**
     * Determines if the provided property expression is a element
     * self-reference.
     * 
     * @param expr
     *            the expression to evaluate
     * @return
     */
    private boolean isSelfReferenceExpression(String expr) {
        return "".equals(expr);
    }
    
    private static final String DYNAMIC_PROPERTY_CHARACTERS = "[\\w.='\"\\|\\%,\\(\\)\\$\\<\\> ]+";
    private static final String NON_NESTED_PROPERTY_CHARACTERS = "[\\w.='\"\\|\\%,\\(\\)\\$\\[\\]\\{\\} ]+";
    
    
    private static final String NESTED_PROPERTY_SPLITTER = "(?!\\:\\{" + DYNAMIC_PROPERTY_CHARACTERS + ")[.](?!" + DYNAMIC_PROPERTY_CHARACTERS
            + "\\})";
    
    private static final String ELEMENT_PROPERTY_SPLITTER = "(?!\\:\\{" + DYNAMIC_PROPERTY_CHARACTERS + ")\\{";
    
    
    /**
     * @param propertyName
     * @return
     */
    protected String[] splitNestedProperty(String propertyName) {
    	return propertyName.split(NESTED_PROPERTY_SPLITTER, 50);
    }
    
    /**
     * @param propertyName
     * @return
     */
    protected String[] splitElementProperty(String propertyName) {
    	return propertyName.split(ELEMENT_PROPERTY_SPLITTER, 2);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.property.PropertyResolverStrategy#getNestedProperty
     * (java.lang.reflect.Type, java.lang.String)
     */
    public NestedProperty getNestedProperty(java.lang.reflect.Type type, String p) {
        return getNestedProperty(type, p, null);
    }
    
    /**
     * @param type
     * @param p
     * @param owner
     * @return
     */
    protected NestedProperty getNestedProperty(java.lang.reflect.Type type, String p, Property owner) {
        
        String typeName = type.toString();
        Property property = null;
        java.lang.reflect.Type propertyType = type;
        final List<Property> path = new ArrayList<Property>();
        final StringBuilder expression = new StringBuilder();
        Property container = owner;
        if (p.indexOf('.') != -1) {
        	final String[] ps;
        	try {
        		ps = splitNestedProperty(p);
        	} catch (StackOverflowError e) {
        		System.out.println("p=" + p);
        		throw e;
        	}
            int i = 0;
            while (i < ps.length) {
                try {
                    property = getProperty(propertyType, ps[i], (i < ps.length - 1), container);
                    propertyType = property.getType();
                    container = null;
                } catch (PropertyNotFoundException e) {
                    throw new PropertyNotFoundException("could not resolve nested property [" + p + "] on " + type + ", because "
                            + e.getLocalizedMessage());
                }
                
                i++;
                if (i < ps.length) {
                    path.add(property);
                    expression.append(property.getExpression() + ".");
                } else {
                    expression.append(property.getExpression());
                }
            }
        }
        
        if (property == null) {
            throw new PropertyNotFoundException(p, type);
        }
        
        return new NestedProperty(expression.toString(), property, path.toArray(new Property[path.size()]));
    }
    
    /**
     * @param type
     * @param p
     * @return
     */
    public Property getElementProperty(java.lang.reflect.Type type, String p) {
        return getElementProperty(type, p, null);
    }
    
    /**
     * @param type
     * @param p
     * @return the Property represented by the specified element property
     *         expression
     */
    @SuppressWarnings("unchecked")
    public Property getElementProperty(java.lang.reflect.Type type, String p, Property owner) {
        
        String[] ps = splitElementProperty(p);
        String elementPropertyExpression = ps[1].substring(0, ps[1].length() - 1);
        
        Property owningProperty;
        if (owner != null) {
            if (type.equals(owner.getType())) {
                owningProperty = owner; 
            } else {
                owningProperty = getProperty(type, ps[0], false, owner);
            }
        } else {
            owningProperty = getProperty(type, ps[0]);
        }
        
        Type<?> elementType;
        
        Property elementProperty;
        if (owningProperty.isMap()) {
            elementType = MapEntry.concreteEntryType((Type<Map<Object, Object>>) owningProperty.getType());
            elementProperty = getProperty(elementType, elementPropertyExpression, false, owningProperty); 
        } else if (owningProperty.isCollection()) {
            elementType = owningProperty.getType().getNestedType(0);
            elementProperty = getProperty(elementType, elementPropertyExpression, false, owningProperty);
        } else if (owningProperty.isArray()) {
            elementType = owningProperty.getType().getComponentType();
            elementProperty = getProperty(elementType, elementPropertyExpression, false, owningProperty);
        } else {
            throw new IllegalArgumentException("'" + p + "' is not a valid element property for " + type);
        }
        
        return new NestedElementProperty(owningProperty, elementProperty, this);
    }
    
    /**
     * @param type
     * @param p
     * @return the Property represented by the specified element property
     *         expression
     */
    @SuppressWarnings("unchecked")
    public Property getIndividualElementProperty(java.lang.reflect.Type type, String p, Property owner) {
        
        String[] ps = p.split("\\[", 2);
        String elementPropertyExpression = ps[1].substring(0, ps[1].length()-1);
        
        Property owningProperty;
        if (owner != null) {
            if (type.equals(owner.getType())) {
                owningProperty = owner; 
            } else {
                owningProperty = getProperty(type, ps[0], false, owner);
            }
        } else {
            owningProperty = getProperty(type, ps[0]);
        }
        
        
        Type<?> elementType;
        Property elementProperty;
        if (owningProperty.isMap()) {
            elementType = MapEntry.concreteEntryType((Type<Map<Object, Object>>) owningProperty.getType());
            String key = elementPropertyExpression.substring(1, elementPropertyExpression.length()-1);
            elementProperty = new MapKeyProperty(key, elementType.getNestedType(1), null); 
        } else if (owningProperty.isCollection()) {
            elementType = owningProperty.getType().getNestedType(0);
            try {
                int index = Integer.valueOf(elementPropertyExpression.replaceAll("[\\[\\]]", ""));
                elementProperty = new ListElementProperty(index, elementType, null); 
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + p + "' is not a valid element property for " + type);
            }
        } else if (owningProperty.isArray()) {
            elementType = owningProperty.getType().getComponentType();
            try {
                int index = Integer.valueOf(elementPropertyExpression);
                elementProperty = new ArrayElementProperty(index, elementType, null); 
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + p + "' is not a valid element property for " + type);
            }
        } else {
            throw new IllegalArgumentException("'" + p + "' is not a valid element property for " + type);
        }
        
        if (!"".equals(owningProperty.getName())) {
            elementProperty = new NestedProperty(p, elementProperty, new Property[]{owningProperty});
        } 
        return elementProperty;
        
    }
    
    
    public Property getProperty(java.lang.reflect.Type type, String expr) {
        
        return getProperty(type, expr, false, null);
    }
    
    public Property getProperty(Property owner, String expr) {
        
        return getProperty(owner.getType(), expr, false, owner);
    }
    
    
    /**
     * Resolves the specified property expression
     * 
     * @param type
     *            the property's owning type
     * @param expr
     *            the property expression to resolve
     * @param properties
     *            the known properties for the type
     * @return the resolved Property
     * @throws MappingException
     *             if the expression cannot be resolved to a property for the
     *             type
     */
    protected Property getProperty(java.lang.reflect.Type type, String expr, boolean isNestedLookup, Property owner) throws MappingException {
        Property property = null;
        
        if (isSelfReferenceExpression(expr)) {
            property = new Property.Builder().name("").getter("").setter(" = %s").type(TypeFactory.valueOf(type)).container(owner).build(this);
        } else if (isNestedPropertyExpression(expr) && !isElementPropertyExpression(expr)) {
            property = getNestedProperty(type, expr, owner);
        } else if (isElementPropertyExpression(expr)) {
            property = getElementProperty(type, expr, owner);
        } else if (isIndividualElementExpression(expr)) {
            property = getIndividualElementProperty(type, expr, owner);
        } else {
            // TODO: perhaps in-line properties should be isolated to a given
            // ClassMapBuilder instance, rather than made available for other
            // mappings
            // of the class; can this cause problems?
            Map<String, Property> inlinePoperties = inlinePropertiesCache.get(type);
            if (inlinePoperties != null) {
                property = inlinePoperties.get(expr);
            }
            if (property == null) {
                Map<String, Property> properties = getProperties(type);
                if (properties.containsKey(expr)) {
                    property = properties.get(expr);
                } else if (isInlinePropertyExpression(expr)) {
                    property = resolveInlineProperty(type, expr);
                    if (property != null) {
                        synchronized (type) {
                            if (inlinePoperties == null) {
                                inlinePoperties = new HashMap<String, Property>(1);
                                inlinePropertiesCache.put(type, inlinePoperties);
                            }
                            inlinePoperties.put(property.getName(), property);
                        }
                    }
                } else {
                    throw new PropertyNotFoundException(expr, type);
                }
                
                if (owner != null) {
                    property = new Property.Builder().merge(property).container(owner).build();
                }
            }
        }
        return property;
    }
    
    private static final Pattern INLINE_PROPERTY_PATTERN = Pattern.compile("([\\w]+)\\:\\{\\s*([\\w\\(\\)'\"\\% ]+)\\s*\\|\\s*([\\w\\(\\)'\"\\%, ]+)\\s*\\|?\\s*(?:(?:type=)([\\w.\\$ \\<\\>]+))?\\}");
    
    /**
     * Determines whether the provided string is a valid in-line property
     * expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return true if the expression represents an in-line property
     */
    protected boolean isInlinePropertyExpression(String expression) {
        return INLINE_PROPERTY_PATTERN.matcher(expression).matches();
    }
    
    /**
     * Resolves in-line properties, which are defined with the following format:<br>
     * "name{getterName|setterName|type=typeName}".<br>
     * <br>
     * Setter name can be omitted, as well as type name; if getter name is
     * omitted, then setter name must be preceded by '|', like so: <br>
     * "name{|setterName|type=typeName}", or like "name{|setterName}". <br>
     * <br>
     * At least one of getter or setter must be provided.<br>
     * Getter or setter 'names' can optionally be the java source of the method
     * call including static arguments, like so: <br>
     * "name{getTheName(\"someString\")|setTheName(\"someString\", %s)}"<br>
     * If the setter is defined in this way, it should contain the string '%s'
     * which represents the value being set.
     * 
     * 
     * 
     * @param type
     * @param expr
     * @return an in-line Property as defined by the provided expression
     */
    public Property resolveInlineProperty(java.lang.reflect.Type type, String expr) {
        Type<?> theType = TypeFactory.valueOf(type);
        Matcher matcher = INLINE_PROPERTY_PATTERN.matcher(expr);
        
        if (matcher.matches()) {
            Property.Builder builder = new Property.Builder(theType, matcher.group(1));
            builder.getter(matcher.group(2));
            builder.setter(matcher.group(3));
            builder.type(matcher.group(4));
            return builder.build(this);
        } else {
            throw new IllegalArgumentException("'" + expr + "' is not a valid dynamic property expression");
        }
    }
    
    /**
     * Collects all properties for the specified type.
     * 
     * @param type
     *            the type for which to collect properties
     * @param referenceType
     *            the reference type for use in resolving generic parameters as
     *            needed
     * @param properties
     *            the properties collected for the current type
     */
    protected abstract void collectProperties(Class<?> type, Type<?> referenceType, Map<String, Property> properties);
    
}
