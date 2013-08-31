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

package ma.glasnost.orika.impl;

import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;

public final class Specifications {
    
    private Specifications() {
        
    }
    
    public interface Specification {
        boolean apply(FieldMap fieldMap);
    }
    
    public static Specification immutable() {
        return IS_IMMUTABLE;
    }
    
    public static Specification toAnEnumeration() {
        return IS_TO_ENUMERATION;
    }
    
    public static Specification anArray() {
        return IS_ARRAY;
    }
    
    public static Specification aCollection() {
        return IS_COLLECTION;
    }
    
    public static Specification aPrimitive() {
        return IS_PRIMITIVE;
    }
    
    public static Specification aPrimitiveToWrapper() {
        return PRIMITIVE_TO_WRAPPER;
    }
    
    public static Specification aWrapperToPrimitive() {
        return WRAPPER_TO_PRIMITIVE;
    }
    
    public static Specification aMapToMap() {
        return MAP_TO_MAP;
    }
    
    public static Specification aMapToArrayOrCollection() {
        return MAP_TO_ARRAY_OR_COLLECTION;
    }
    
    public static Specification aMapToArray() {
        return MAP_TO_ARRAY;
    }
    
    public static Specification aMapToCollection() {
        return MAP_TO_COLLECTION;
    }
    
    public static Specification anArrayOrCollectionToMap() {
        return ARRAY_OR_COLLECTION_TO_MAP;
    }
    
    public static Specification aBeanToMap() {
        return BEAN_TO_MAP;
    }
    
    public static Specification aBeanToArrayOrList() {
        return BEAN_TO_ARRAY_OR_LIST;
    }
    
    public static Specification aMapToBean() {
        return MAP_TO_BEAN;
    }
    
    public static Specification anArrayOrListToBean() {
        return ARRAY_OR_LIST_TO_BEAN;
    }
    
    public static Specification aMappingOfTheRequiredClassProperty() {
        return OBJECT_CLASS_PROPERTY;
    }
    
    public static Specification aMultiOccurrenceElementMap() {
        return MULTI_OCCURRENCE_ELEMENT;
    }
    
    /**
     * @return true if this field map specifies a mapping from a String type
     *         field to another field which has a static valueOf method which
     *         allows parsing the field from a string.
     */
    public static Specification aStringToPrimitiveOrWrapper() {
        return STRING_TO_PRIMITIVE_OR_WRAPPER;
    }
    
    public static Specification aConversionToString() {
        return CONVERSION_TO_STRING;
    }
    
    private static final Specification IS_IMMUTABLE = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return ClassUtil.isImmutable(fieldMap.getSource().getType())
                    && fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
        }
    };
    
    private static final Specification IS_TO_ENUMERATION = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getDestination().getType().isEnum()
                    && (fieldMap.getSource().getType().getRawType().equals(String.class) || fieldMap.getSource().getType().isEnum());
        }
    };
    
    private static final Specification IS_ARRAY = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection());
        }
    };
    
    private static final Specification IS_COLLECTION = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection()) && fieldMap.getDestination().isCollection();
        }
    };
    
    private static final Specification IS_PRIMITIVE = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().getType().isPrimitive() || fieldMap.getDestination().getType().isPrimitive();
        }
    };
    
    private static final Specification WRAPPER_TO_PRIMITIVE = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getDestination().isPrimitive() && fieldMap.getSource().getType().isPrimitiveWrapper();
        }
        
    };
    
    private static final Specification PRIMITIVE_TO_WRAPPER = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getDestination().getType().isPrimitiveWrapper() && fieldMap.getSource().isPrimitive();
        }
        
    };
    
    private static final Specification STRING_TO_PRIMITIVE_OR_WRAPPER = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return String.class.equals(fieldMap.getSource().getType().getRawType())
                    && (fieldMap.getDestination().getType().isPrimitive() || fieldMap.getDestination().getType().isPrimitiveWrapper());
        }
        
    };
    
    private static final Specification CONVERSION_TO_STRING = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return String.class.equals(fieldMap.getDestination().getType().getRawType());
        }
        
    };
    
    private static final Specification MAP_TO_MAP = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().isMap() && fieldMap.getDestination().isMap();
        }
    };
    
    private static final Specification MAP_TO_ARRAY_OR_COLLECTION = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().isMap() && (fieldMap.getDestination().isCollection() || fieldMap.getDestination().isArray());
        }
    };
    
    private static final Specification MAP_TO_ARRAY = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().isMap() && fieldMap.getDestination().isArray();
        }
    };
    
    private static final Specification MAP_TO_COLLECTION = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().isMap() && fieldMap.getDestination().isCollection();
        }
    };
    
    private static final Specification MAP_TO_BEAN = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().isMapKey()
                    && (ClassUtil.isImmutable(fieldMap.getDestination().getType()) || (!fieldMap.getDestination().isCollection()
                            && !fieldMap.getDestination().isArray() && !fieldMap.getDestination().isMap() && !fieldMap.getDestination()
                            .isEnum()));
        }
    };
    
    private static final Specification ARRAY_OR_LIST_TO_BEAN = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return (fieldMap.getSource().isListElement() || fieldMap.getSource().isArrayElement())
                    && (ClassUtil.isImmutable(fieldMap.getDestination().getType()) || (!fieldMap.getDestination().isCollection()
                            && !fieldMap.getDestination().isArray() && !fieldMap.getDestination().isMap() && !fieldMap.getDestination()
                            .isEnum()));
        }
    };
    
    private static final Specification ARRAY_OR_COLLECTION_TO_MAP = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getDestination().isMap() && (fieldMap.getSource().isCollection() || fieldMap.getSource().isArray());
        }
    };
    
    private static final Specification BEAN_TO_MAP = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            
            return fieldMap.getDestination().isMapKey()
                    && (ClassUtil.isImmutable(fieldMap.getSource().getType()) || (!fieldMap.getSource().isCollection()
                            && !fieldMap.getSource().isArray() && !fieldMap.getSource().isMap() && !fieldMap.getSource().isEnum()));
        }
    };
    
    private static final Specification BEAN_TO_ARRAY_OR_LIST = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            
            return (fieldMap.getDestination().isListElement() || fieldMap.getDestination().isArrayElement())
                    && (ClassUtil.isImmutable(fieldMap.getSource().getType()) || (!fieldMap.getSource().isCollection()
                            && !fieldMap.getSource().isArray() && !fieldMap.getSource().isMap() && !fieldMap.getSource().isEnum()));
        }
    };
    
    private static final Specification OBJECT_CLASS_PROPERTY = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return "class".equals(fieldMap.getSource().getName()) && "class".equals(fieldMap.getDestination().getName())
                    && Class.class.equals(fieldMap.getSource().getRawType());
            
        }
    };
    
    private static final Specification MULTI_OCCURRENCE_ELEMENT = new Specification() {
        
        public boolean apply(FieldMap fieldMap) {
            return fieldMap.getSource().getContainer() != null || fieldMap.getDestination().getContainer() != null;
        }
    };
}
