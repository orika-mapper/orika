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
package ma.glasnost.orika.impl.generator;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Filter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * VariableRef represents a reference to a given variable or property; it
 * contains various helper methods to properly set it's value and interrogate
 * it's underlying property or type. It also returns a properly type-safe cast
 * of it as the toString() method, so it can safely be used directly as a
 * replacement parameter for source code statements.
 * 
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class VariableRef {
    
    protected String name;
    private Property property;
    private VariableRef owner;
    private Type<?> type;
    private boolean declared;
    private Converter<?, ?> converter;
    private Filter<?, ?> filter;
    private boolean nullPossible;
    private boolean nullPathPossible;
    
    public VariableRef(Property property, String name) {
        this.name = name;
        this.property = property;
        this.type = property.getType();
        this.nullPossible = !isPrimitive();
        this.nullPathPossible = isNestedProperty();
    }
    
    public VariableRef(Property property, VariableRef anchor) {
        this(property, anchor.toString());
    }
    
    public VariableRef(Type<?> type, String name) {
        this.name = name;
        this.type = type;
        this.nullPossible = !isPrimitive();
        this.nullPathPossible = isNestedProperty();
    }
    
    public void setConverter(Converter<?, ?> converter) {
        this.converter = converter;
    }
    
    public Converter<?, ?> getConverter() {
        return converter;
    }
    
    public Filter<?, ?> getFilter() {
        return filter;
    }

    public void setFilter(Filter<?, ?> filter) {
        this.filter = filter;
    }

    public void setOwner(VariableRef owner) {
        this.owner = owner;
    }
    
    public VariableRef getOwner() {
        return owner;
    }
    
    protected String getter() {
        return property == null ? name : getGetter(property, name);
    }
    
    protected String setter() {
        return property == null ? name + " = %s" : getSetter(property, name);
    }
    
    public boolean isReadable() {
        return getter() != null;
    }
    
    public boolean isAssignable() {
        return setter() != null;
    }
    
    public Class<?> rawType() {
        return type.getRawType();
    }
    
    /**
     * @return the Property (if any) associated with this VariableRef
     */
    public Property property() {
        return property;
    }
    
    public Type<?> type() {
        return type;
    }
    
    public boolean isPrimitive() {
        return type.isPrimitive();
    }
    
    public boolean isArray() {
        return property != null ? property.isArray() : type.getRawType().isArray();
    }
    
    public boolean isCollection() {
        return property != null ? property.isCollection() : Collection.class.isAssignableFrom(rawType());
    }
    
    public boolean isList() {
        return property != null ? property.isList() : List.class.isAssignableFrom(rawType());
    }
    
    public boolean isSet() {
        return property != null ? property.isSet() : Set.class.isAssignableFrom(rawType());
    }
    
    public boolean isMap() {
        return property != null ? property.isMap() : Map.class.isAssignableFrom(rawType());
    }
    
    public boolean isMapEntry() {
        return Entry.class.isAssignableFrom(rawType());
    }
    
    public boolean isWrapper() {
        return type.isPrimitiveWrapper();
    }
    
    public String wrapperTypeName() {
        return ClassUtil.getWrapperType(rawType()).getCanonicalName();
    }
    
    public VariableRef elementRef(String name) {
        return new VariableRef(elementType(), name);
    }
    
    public String elementTypeName() {
        return elementType() != null ? elementType().getCanonicalName() : null;
    }
    
    public Type<?> elementValueType() {
        if (type.getRawType().isArray()) {
            return type.getComponentType();
        } else if (type.isMap()) {
            return type.getNestedType(1);
        } else {
            return type.getNestedType(0);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Type<?> elementType() {
        
        if (type.getRawType().isArray()) {
            return type.getComponentType();
        } else if (type.isMap()) {
            return MapEntry.concreteEntryType((Type<Map<Object, Object>>) type);
        } else {
            return property != null ? property.getElementType() : ((Type<?>) type.getActualTypeArguments()[0]);
        }
    }
    
    public Type<?> mapKeyType() {
        if (isMap()) {
            return type().getNestedType(0);
        }
        return null;
    }
    
    public Type<?> mapValueType() {
        if (isMap()) {
            return type().getNestedType(1);
        }
        return null;
    }
    
    public String typeName() {
        return type.getCanonicalName();
    }
    
    public String asWrapper() {
        String ref = getter();
        if (isPrimitive()) {
            ref = ClassUtil.getWrapperType(rawType()).getCanonicalName() + ".valueOf(" + ref + ")";
        }
        return ref;
    }
    
    /**
     * Generates code to perform assignment to this VariableRef.
     * 
     * @param value
     * @param replacements
     * @return
     */
    public String assign(String value, Object... replacements) {
        if (setter() != null) {
            return assignIfPossible(value, replacements);
        } else {
            throw new IllegalArgumentException("Attempt was made to generate assignment/setter code for [" + name + "."
                    + (property != null ? property : type) + "] which has no setter/assignment method");
        }
    }
    
    /**
     * Generates code to perform assignment to this VariableRef, if it is
     * assignable.
     * 
     * @param value
     * @param replacements
     * @return
     */
    public String assignIfPossible(String value, Object... replacements) {
        if (setter() != null) {
            String expr = format(value, replacements);
            expr = cast(expr);
            return format(setter(), expr);
        } else {
            return "";
        }
    }
    
    /**
     * Generates code to perform assignment to this VariableRef, if it is
     * assignable.
     * 
     * @param value
     * @param replacements
     * @return
     */
    public String assignIfPossible(VariableRef value) {
        if (setter() != null) {
            return format(setter(), cast(value));
        } else {
            return "";
        }
    }
    
    /**
     * Returns java code which assigns the value of the provided PropertyRef to
     * this PropertyRef
     * 
     * @param value
     * @return
     */
    public String assign(VariableRef value) {
        if (setter() != null) {
            String expr = value.toString();
            if (value.type().isPrimitive() && type.isPrimitiveWrapper()) {
                String wrapperClass = ClassUtil.getWrapperType(rawType()).getCanonicalName();
                expr = format("(%s) %s.valueOf(%s)", wrapperClass, wrapperClass, expr);
            } else if (type.isPrimitive() && value.type().isPrimitiveWrapper()) {
                expr = format("((%s)%s).%sValue()", ClassUtil.getWrapperType(rawType()).getCanonicalName(), expr, primitiveType(rawType()));
            } else if (type.isArray() && value.isCollection()) {
                if (type.getComponentType().isPrimitive() && value.elementType().isPrimitiveWrapper()) {
                    String wrapperType = ClassUtil.getWrapperType(rawType().getComponentType()).getCanonicalName();
                    expr = format("(%s[])%s.toArray(new %s[0])", wrapperType, expr, wrapperType);
                } else {
                    expr = format("(%s[])%s.toArray(new %s[0])", value.elementType().getCanonicalName(), expr, value.elementType()
                            .getCanonicalName());
                }
            } else if (!type.isArray() && value.type().isPrimitive() && !type.isPrimitive()) {
                expr = format("%s.valueOf(%s)", value.type.getWrapperType().getCanonicalName(), expr);
            }
            return format(setter(), expr);
        } else {
            return "";
        }
    }
    
    public String cast(VariableRef ref) {
        return cast(ref, type());
    }
    
    /**
     * Returns Java code which provides a cast of the specified value to the
     * type of this property ref
     * 
     * @param value
     * @return
     */
    public String cast(String value) {
        return cast(value, type());
    }
    
    /**
     * Returns Java code which provides a cast of the specified value to the
     * type of this property ref
     * 
     * @param value
     * @return
     */
    protected static String cast(String value, Type<?> type) {
        String castValue = value.trim();
        String typeName = type.getCanonicalName();
        /*
         * Avoid double-cast
         */
        if (!"null".equals(value)) {
            if (type.isPrimitive()) {
                if (type.getRawType() == Character.TYPE) {
                    castValue = format("(\"\"+%s).charAt(0)", castValue);
                } else if (!isPrimitiveLiteral(castValue, type)) {
                    castValue = format("%s.valueOf(\"\"+%s).%sValue()", type.getWrapperType().getCanonicalName(), castValue, type);
                }
            } else if (type.isPrimitiveWrapper() && isPrimitiveLiteral(castValue, type)) {
                castValue = format("%s.valueOf(%s)", type.getWrapperType().getCanonicalName(), castValue);
            } else if (type.isString()) {
                castValue = "\"\" + " + castValue;
            } else if (!value.startsWith("(" + typeName + ")") && !value.startsWith("((" + typeName + ")")) {
                castValue = "((" + typeName + ")" + castValue + ")";
            }
        }
        return castValue;
    }
    
    /**
     * Returns Java code which provides a cast of the specified value to the
     * type of this property ref
     * 
     * @param value
     * @return
     */
    protected static String cast(VariableRef value, Type<?> type) {
        String castValue = value.toString();
        String typeName = type.getCanonicalName();
        
        if (type.isPrimitive()) {
            if (value.isWrapper()) {
                castValue = format("%s.%sValue()", castValue, type);
            } else if (Character.TYPE == type.getRawType() && value.type().isString()) { 
                castValue = format("%s.charAt(0)", value);
            } else if (!value.isPrimitive()) {
                castValue = format("%s.valueOf(\"\"+%s).%sValue()", type.getWrapperType().getCanonicalName(), castValue, typeName);
            }
        } else if (type.isPrimitiveWrapper()) {
            if (value.isPrimitive()) {
                castValue = format("%s.valueOf(%s)", type.getCanonicalName(), castValue);
            }
        } else if (type.isString() && !value.type().isString()) {
            castValue = "\"\" + " + castValue;
        } else if (!castValue.replace("(", "").startsWith(typeName)){
            castValue = "((" + typeName + ")" + castValue + ")";
        }
        return castValue;
    }
    
    /**
     * Returns Java code which declares this variable, initialized with it's
     * default value.
     * 
     * @return the code which declares this variable, and explicitly assigns
     *         it's default value.
     */
    public String declare() {
        declared = true;
        return format("\n%s %s = %s", typeName(), name(), getDefaultValue(rawType()));
    }
    
    /**
     * Returns Java code which declares this variable, initialized with the
     * provided value.
     * 
     * @param value
     *            the value to assign
     * @param args
     *            any replacement arguments to applied to value via
     *            String.format()
     * @return the code which declares this variable, and explicitly assigns the
     *         provided value.
     */
    public String declare(String value, Object... args) {
        String valueExpr = format(value, args);
        valueExpr = cast(valueExpr);
        declared = true;
        return format("\n%s %s = %s", typeName(), name(), valueExpr);
    }
    
    /**
     * Returns Java code which declares this variable, initialized with the
     * provided value.
     * 
     * @param value
     *            the value to assign
     * @param args
     *            any replacement arguments to applied to value via
     *            String.format()
     * @return the code which declares this variable, and explicitly assigns the
     *         provided value.
     */
    public String declare(VariableRef ref) {
        return declare(cast(ref));
    }
    
    public boolean isDeclared() {
        return declared;
    }
    
    /**
     * Returns the Java code which represents the default value for the
     * specified type
     * 
     * @param clazz
     * @return
     */
    public static String getDefaultValue(Class<?> clazz) {
        if (Boolean.TYPE.equals(clazz))
            return "false";
        else if (Character.TYPE.equals(clazz))
            return "'\\u0000'";
        else if (clazz.isPrimitive())
            return "0";
        else
            return "null";
    }
    
    public String primitiveType() {
        return primitiveType(rawType());
    }
    
    public String primitiveType(Class<?> clazz) {
        String type = clazz.getSimpleName().toLowerCase();
        if ("integer".equals(type)) {
            type = "int";
        } else if ("character".equals(type)) {
            type = "char";
        }
        return type;
    }
    
    public String owner() {
        return name;
    }
    
    public String name() {
        return property != null && !"".equals(property.getName()) ? property.getName() : name;
    }
    
    public String isNull() {
        return property != null ? isNull(property, name) : getter() + " == null";
    }
    
    /**
     * Removes the outermost property from a nested getter expression
     * 
     * @param expression
     * @return
     */
    private static String unwrap(String expression) {
        // TODO: should write a getParentGetter() instead of unwrapping
        if (expression.startsWith("((")) {
            expression = expression.substring(expression.indexOf(")") + 1, expression.length() - 1);
            if (expression.endsWith("]")) {
                expression = expression.substring(0, expression.lastIndexOf("["));
            } else {
                expression = expression.substring(0, expression.lastIndexOf("."));
            }
        }
        return expression;
    }
    
    private static String isNull(Property property, String name) {
        if (property == null) {
            return name + " == null";
        } else {
            String getterNull = getGetter(property, name) + " == null";
            if (property.isListElement()) {
                return "(" + unwrap(getGetter(property, name)) + ".size() <= " + property.getName().replaceAll("[\\[\\]]", "") + " || "
                        + getterNull + ")";
            } else if (property.isArrayElement()) {
                return "(" + unwrap(getGetter(property, name)) + ".length <= " + property.getName().replaceAll("[\\[\\]]", "") + " || "
                        + getterNull + ")";
            } else {
                return getterNull;
            }
        }
    }
    
    /**
     * @return a nested-property safe null check for this property
     */
    private String notNullIncludingPath() {
        StringBuilder path = new StringBuilder();
        path.append("(");
        if (property() != null && property().hasPath()) {
            boolean first = true;
            
            String expression = this.name;
            
            for (final Property p : property().getPath()) {
                if (!first) {
                    path.append(" && ");
                } else {
                    first = false;
                }
                
                path.append(format("!(%s)", isNull(p, expression)));
                expression = getGetter(p, expression);
            }
        }
        if (path.length() > 1) {
            path.append(" && ");
        }
        path.append(format("!(%s)", isNull(property, name)));
        path.append(")");
        
        return path.toString();
    }
    
    public String notNull() {
        return notNull(isNullPathPossible());
    }
    
    public String notNull(boolean includePath) {
        if (includePath) {
            return notNullIncludingPath();
        } else {
            return format("!(%s)", isNull(property, name));
        }
    }
    
    public String ifNotNull() {
        return ifNotNull(isNullPathPossible());
    }
    
    public String ifNotNull(boolean includePath) {
        return "if ( " + notNull(includePath) + ")";
    }
    
    public String ifNull() {
        return "if ( " + isNull() + ") ";
    }
    
    public String toString() {
        return getter();
    }
    
    /**
     * @return whether or not this VariableRef represents a nested property
     */
    public boolean isNestedProperty() {
        return property != null && property.hasPath();
    }
    
    public List<VariableRef> getPath() {
        if (property != null && property.hasPath()) {
            Property[] propPath = property.getPath();
            List<VariableRef> path;
            if (propPath.length > 1) {
                
                path = new ArrayList<VariableRef>(propPath.length);
                path.add(new VariableRef(propPath[0], name));
                for (int i = 1; i < propPath.length; ++i) {
                    Property[] nestedPath = new Property[i];
                    System.arraycopy(propPath, 0, nestedPath, 0, i);
                    path.add(new VariableRef(new NestedProperty(join(propPath, ".", 0, i + 1), propPath[i], nestedPath), name));
                }
            } else {
                path = Collections.singletonList(new VariableRef(propPath[0], name));
            }
            return path;
        } else {
            return Collections.emptyList();
        }
    }
    
    private static String join(Property[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }
        
        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuilder buf = new StringBuilder(bufSize);
        
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i].getName());
            }
        }
        return buf.toString();
    }
    
    public String assertType() {
        return "if(!(" + name + " instanceof " + typeName() + ")) throw new IllegalStateException(\"[" + name + "] is not an instance of "
                + typeName() + " \");";
    }
    
    /**
     * Generates java code for a reference to the "size" of this VariableRef
     * 
     * @return
     */
    public String size() {
        return getter() + "." + (rawType().isArray() ? "length" : "size()");
    }
    
    protected static String getGetter(final Property property, String variableExpression) {
        if (property.getGetter() == null)
            return null;
        String var = variableExpression;
        if (property.hasPath()) {
            for (final Property p : property.getPath()) {
                var = getGetter(p, var);
            }
        }
        String getter = "((" + property.getType().getCanonicalName() + ")" + var;
        if (!property.isArrayElement() && !"".equals(property.getName()) && !property.getGetter().startsWith("[")) {
            getter += "." + property.getGetter() + ")";
        } else {
            getter += property.getGetter() + ")";
        }
        return getter;
    }
    
    public String isInstanceOf(Type<?> type) {
        return format("(%s instanceof %s)", getter(), type.getCanonicalName());
    }
    
    /**
     * Returns a fully type-cast setter for the property which has no reliance
     * on java generics.
     * 
     * @param property
     *            the Property for which to return the getter
     * @param variableExpression
     *            the String value to use for the variable on which the getter
     *            is called
     * @return
     */
    protected static String getSetter(final Property property, final String variableExpression) {
        if (property.getSetter() == null)
            return null;
        
        String var = variableExpression;
        if (property.hasPath()) {
            for (final Property p : property.getPath()) {
                var = getGetter(p, var);
            }
        }
        
        return var + (property.isArrayElement() || "".equals(property.getName()) || property.getSetter().startsWith("[") ? "" : ".")
                + property.getSetter();
    }
    
    /**
     * @return true if it is possible for this variable to be null at the
     * current state within code
     */
    public boolean isNullPossible() {
        return nullPossible;
    }
    
    /**
     * Used to mark that this variable can not possibly be null at the 
     * current state within code (because null has already been checked)
     */
    public void setNullImpossible() {
        this.nullPossible = false;
    }
    
    /**
     * @return true if it is possible for this variable to be null at the
     * current state within code
     */
    public boolean isNullPathPossible() {
        return nullPathPossible;
    }
    
    /**
     * Used to mark that this variable can not possibly be null at the 
     * current state within code (because null has already been checked)
     */
    public void setNullPathImpossible() {
        this.nullPathPossible = false;
    }
    
    /**
     * Return Java code which avoids a NullPointerException when accessing this
     * variable reference; if it is not backed by a nested property, this method
     * returns the empty string.
     * 
     * @return
     */
    public String pathNotNull() {
        StringBuilder path = new StringBuilder();
        if (property.hasPath()) {
            boolean first = true;
            path.append("(");
            String expression = this.name;
            
            for (final Property p : property.getPath()) {
                if (!first) {
                    path.append(" && ");
                } else {
                    first = false;
                }
                path.append(format("!(%s)", isNull(p, expression)));
                expression = getGetter(p, expression);
            }
            path.append(")");
        }
        return path.toString();
    }
    
    /**
     * Return Java code which avoids a NullPointerException when accessing this
     * variable reference; if it is not backed by a nested property, this method
     * returns the empty string.
     * 
     * @return
     */
    public String ifPathNotNull() {
        if (nullPathPossible) {
            return "if " + pathNotNull();
        } else {
            return "";
        }
    }
    
    protected static boolean isPrimitiveLiteral(String expr, Type<?> type) {
        if (type.isPrimitive()) {
            String primitiveType = type.getCanonicalName();
            if ("boolean".equals(primitiveType)) {
                return expr.matches("(true|false)");
            } else if ("char".equals(primitiveType)) {
                return expr.matches("^'\\w+'$");
            } else if ("int".equals(primitiveType) || "short".equals(primitiveType)) {
                return expr.matches("\\d+");
            } else if ("long".equals(primitiveType)) {
                return expr.matches("\\d+(l|L)?");
            } else if ("float".equals(primitiveType)) {
                return expr.matches("\\d*(\\.\\d*)?(f|F)?");
            } else if ("double".equals(primitiveType)) {
                return expr.matches("\\d+(\\.\\d*)?");
            }
            
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VariableRef other = (VariableRef) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (property == null) {
            if (other.property != null) {
                return false;
            }
        } else if (!property.equals(other.property)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
    
}
