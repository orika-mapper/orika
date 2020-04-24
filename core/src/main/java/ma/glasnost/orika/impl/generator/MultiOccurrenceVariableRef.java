/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.impl.DefaultConcreteTypeMap;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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
public class MultiOccurrenceVariableRef extends VariableRef {
    
    private String iteratorName;
    private boolean iteratorDeclared;
    
    /**
     * up-converts the specified standard VariableRef into a
     * MultiOccurrenceVariableRef
     * 
     * @param r
     * @return
     */
    public static MultiOccurrenceVariableRef from(VariableRef r) {
        if (r.property() != null) {
            return new MultiOccurrenceVariableRef(r.property(), r.name);
        } else {
            return new MultiOccurrenceVariableRef(r.type(), r.name);
        }
    }
    
    public MultiOccurrenceVariableRef(Property property, String name) {
        super(property, name);
    }

    public MultiOccurrenceVariableRef(Type<?> type, String name) {
        super(type, name);
    }
    
    public String declareIterator() {
        return declareIterator(getter());
    }

    public String declareIterator(String value) {
        if (iteratorDeclared) {
            throw new IllegalStateException("Iterator has already been declared");
        }
        String iterator;
        if (isArray()) {
            iterator = "int " + getIteratorName() + " = -1";
        } else if (isMap()) {
            iterator = new EntrySetRef(this, name()).declareIterator();
        } else {
            iterator = "java.util.Iterator " + getIteratorName() + " = " + value + ".iterator()";
        }
        iteratorDeclared = true;
        return iterator;
    }

    public String nextElement() {
        if (!iteratorDeclared) {
            throw new IllegalStateException("Iterator has not been declared");
        }
        String next;
        if (isArray()) {
            next = getter() + "[++" + getIteratorName() + "]";
        } else {
            next = getIteratorName() + ".next()";
        }
        return next;
    }

    public VariableRef nextElementRef() {
        return new VariableRef(elementType(), nextElement());
    }

    public String iteratorHasNext() {
        if (!iteratorDeclared) {
            throw new IllegalStateException("Iterator has not been declared");
        }
        String hasNext;
        if (isArray()) {
            hasNext = getIteratorName() + " < (" + getter() + ".length - 1)";
        } else {
            hasNext = getIteratorName() + ".hasNext()";
        }
        return hasNext;
    }

    public String notEmpty() {
        if (isArray()) {
            return getter() + ".length > 0";
        } else {
            return "!" + getter() + ".isEmpty()";
        }
    }

    public String add(String value) {

        if (isArray()) {
            if (!iteratorDeclared) {
                throw new IllegalStateException("Iterator must be declared in order to add elements to destination array");
            }
            return getter() + "[++" + getIteratorName() + "] = " + value;
        } else if (isMap()) {
            return getter() + ".put(" + value + ".getKey(), " + value + ".getValue())";
        } else if (isCollection()) {
            return getter() + ".add(" + cast(value, type().getNestedType(0)) + ")";
        } else {
            throw new IllegalArgumentException(type() + " does not support adding of elements");
        }
    }

    public String add(VariableRef value) {

        if (isArray()) {
            if (!iteratorDeclared) {
                throw new IllegalStateException("Iterator must be declared in order to add elements to destination array");
            }
            return getter() + "[++" + getIteratorName() + "] = " + value;
        } else if (isMap() && value.isMapEntry()) {
            return getter() + ".put(" + value + ".getKey(), " + value + ".getValue())";
        } else if (isCollection()) {
            return getter() + ".add(" + cast(value, type().getNestedType(0)) + ")";
        } else {
            throw new IllegalArgumentException(type() + " does not support adding elements of type " + value.type());
        }
    }

    /**
     * A convenience function for adding all of one multi-occurrence type to another
     *
     * @param value
     * @return
     */
    public String addAll(VariableRef value) {
        String assignment = addAllByAssign(value);
        return assignment != null ? assignment : addAllButNoAssign(value);
    }

    /**
     * A convenience function which adds all of one multi-occurence type to another variable, but without any assignment
     *
     * @param value
     * @return
     */
    public String addAllButNoAssign(VariableRef value) {
        if (isMap() && value.isList()) {
            return String.format("listToMap(%s, %s)", value, this);
        } else if (isCollection() && value.isArray()) {
            if (value.type().getComponentType().isPrimitive()) {
                return getter() + ".addAll(asList(" + value + "))";
            } else {
                return getter() + ".addAll(java.util.Arrays.asList(" + value + ")";
            }
        } else if (isMap() && value.isMap()) {
            return getter() + ".putAll(" + value + ")";
        } else {
            return getter() + ".addAll(" + value + ")";
        }
    }

    public String collectionType() {
        String collection;
        if (isList()) {
            collection = "List";
        } else if (isSet()) {
            collection = "Set";
        } else if (isCollection()) {
            // TODO By default we create an ArrayList for collections
            // Can we introduce a CollectionFactory to enable the user to choose
            // his own collections
            collection = "List";
        } else {
            throw new IllegalStateException(type() + " is not a collection type");
        }
        return collection;
    }

    public String newCollection() {
        return newInstance("");
    }

    public String newInstance(String sizeExpr) {
        if (isArray()) {
            return "new " + rawType().getComponentType().getCanonicalName() + "[" + sizeExpr + "]";
        } else {
            if (type().isConcrete()) {
                return newInstance(type().getRawType());
            }
            else {
                return newInstance(DefaultConcreteTypeMap.get(type().getRawType()));
            }
        }
    }

    public String newMap() {
        if (SortedMap.class.isAssignableFrom(rawType())) {
            return "new java.util.TreeMap()";
        }
        return "new java.util.LinkedHashMap(" + "" + ")";
    }

    /**
     * Generates java code for a reference to the "size" of this VariableRef
     *
     * @return
     */
    public String size() {
        return getter() + "." + (rawType().isArray() ? "length" : "size()");
    }

    private String addAllByAssign(VariableRef value) {
        if (isArray() && value.isCollection()) {
            if (type().getComponentType().isPrimitive()) {
                return assign("%sArray(%s)", type().getComponentType().getCanonicalName(), value);
            } else {
                return assign("listToArray(%s, %s.class)", value, type().getCanonicalName());
            }
        } else if (isMap() && value.isList() && isAssignable()) {
            return assign("listToMap(%s, java.util.LinkedHashMap.class)", value);
        }
        return null;
    }

    private String getIteratorName() {
        if (iteratorName == null) {
            String prefix = this.name();
            if (this.property() != null && this.property().getName().matches("[\\w$]+")) {
                prefix = this.property().getName();
            }
            if ("".equals(prefix) && !"".equals(this.name)) {
                prefix = this.name;
            }

            if (isArray()) {
                iteratorName = prefix + "_$_index";
            } else {
                iteratorName = prefix + "_$_iter";
            }
        }
        return iteratorName;
    }

    private String newInstance(Class<?> type) {
        try {
            if (type.getConstructor() != null) {
                return "new " + type.getCanonicalName() + "()";
            }
            else {
                throw new IllegalStateException(type + " constructor is null");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(type + " has no default constructor");
        } catch (SecurityException e) {
            throw new IllegalStateException(type + " cannot be instanced");
        }
    }

    private static class EntrySetRef extends MultiOccurrenceVariableRef {

        private String name;
        
        public EntrySetRef(VariableRef sourceMap, String variableName) {
            super(getSourceEntryType(sourceMap), sourceMap + ".entrySet()");
            this.name = variableName;
        }
        
        @SuppressWarnings("unchecked")
        private static Type<?> getSourceEntryType(VariableRef sourceMap) {
            return TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) sourceMap.type()));
        }
        
        public String name() {
            if (this.name != null) {
                return this.name;
            } else {
                return super.name();
            }
        }
    }
    
}
