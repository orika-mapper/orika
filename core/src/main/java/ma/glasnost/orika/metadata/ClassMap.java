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

package ma.glasnost.orika.metadata;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.Mapper;

/**
 * ClassMap represents a mapping association from one type to another.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMap<A, B> implements MappedTypePair<A, B>{
    
    private final Type<A> aType;
    private final Type<B> bType;
    private final Set<FieldMap> fieldsMapping;
    private final Set<MapperKey> usedMappers;
    
    private final Mapper<A, B> customizedMapper;
    
    private final String[] constructorA;
    private final String[] constructorB;
    private final MapperKey mapperKey;
    
    private final Boolean sourcesMappedOnNull;
    private final Boolean destinationsMappedOnNull;
    
    /**
     * Constructs a new ClassMap
     * 
     * @param aType the 'A' type
     * @param bType the 'B' type
     * @param fieldsMapping the specific mapping of the fields from type 'A' to type 'B' and vise-versa
     * @param customizedMapper the customized mapper that should be used
     * @param usedMappers the set of mappers used by this mapper to map ancestors' fields
     * @param constructorA a description of the parameter names of the constructor to use for type 'A'
     * @param constructorB a description of the parameter names of the constructor to use for type 'B'
     */
    public ClassMap(Type<A> aType, Type<B> bType, Set<FieldMap> fieldsMapping, Mapper<A, B> customizedMapper, Set<MapperKey> usedMappers,
            String[] constructorA, String[] constructorB, Boolean sourcesMappedOnNull, Boolean destinationsMappedOnNull) {
        this.aType = aType;
        this.bType = bType;
        
        this.customizedMapper = customizedMapper;
        
        this.fieldsMapping = Collections.unmodifiableSet(fieldsMapping);
        this.usedMappers = Collections.unmodifiableSet(usedMappers);
        
        this.mapperKey = new MapperKey(aType, bType);
        
        this.sourcesMappedOnNull = sourcesMappedOnNull;
        this.destinationsMappedOnNull = destinationsMappedOnNull;
        
        if (constructorA != null) {
            this.constructorA = constructorA.clone();
        } else {
            this.constructorA = null;
        }
        
        if (constructorB != null) {
            this.constructorB = constructorB.clone();
        } else {
            this.constructorB = null;
        }
    }
    
    public ClassMap<A,B> copy(Set<FieldMap> fieldsMapping) {
        Set<MapperKey> usedMappers = new LinkedHashSet<MapperKey>();
        usedMappers.addAll(this.usedMappers);
        String[] constructorA = this.constructorA == null ? null : this.constructorA.clone();
        String[] constructorB = this.constructorB == null ? null : this.constructorB.clone();
        
        return new ClassMap<A,B>(aType, bType, fieldsMapping, customizedMapper, usedMappers, constructorA, constructorB, sourcesMappedOnNull, destinationsMappedOnNull);
    }
    
    public MapperKey getMapperKey() {
        return mapperKey;
    }
    
    /**
     * @param fieldMap
     */
    public void addFieldMap(FieldMap fieldMap) {
        fieldsMapping.add(fieldMap);
    }
    
    /**
     * @return the 'A' type for the mapping
     */
    public Type<A> getAType() {
        return aType;
    }
    
    /**
     * @return the 'B' type for the mapping
     */
    public Type<B> getBType() {
        return bType;
    }
    
    /**
     * @return the mapping of fields between the two types of this mapping
     */
    public Set<FieldMap> getFieldsMapping() {
        return fieldsMapping;
    }
    
    /**
     * @return the name of the 'A' type
     */
    public String getATypeName() {
        return aType.getSimpleName();
    }
    
    /**
     * @return the name of the 'B' type
     */
    public String getBTypeName() {
        return bType.getSimpleName();
    }
    
    /**
     * @return the customized Mapper to be used for this mapping
     */
    public Mapper<A, B> getCustomizedMapper() {
        return customizedMapper;
    }
    
    /**
     * @return
     */
    public String getMapperClassName() {
        return "Orika_" + getBTypeName() + "_" + getATypeName() + "_Mapper";
    }
    
    /**
     * @return
     */
    public String[] getConstructorA() {
        return constructorA;
    }
    
    /**
     * @return
     */
    public String[] getConstructorB() {
        return constructorB;
    }
    
    
    
    /**
     * @return sourcesMappedOnNull for this ClassMap; can be null, which indicates that
     * no preference is specified, and the global default should be used
     */
    public Boolean areSourcesMappedOnNull() {
        return sourcesMappedOnNull;
    }

    /**
     * @return destinationsMappedOnNull for this ClassMap; can be null, which indicates
     * that no preference is specified, and the global default should be used
     */
    public Boolean areDestinationsMappedOnNull() {
        return destinationsMappedOnNull;
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = result + (aType == null ? 0 : aType.hashCode());
        result = result + (bType == null ? 0 : bType.hashCode());
        return result;
    }
    
    public Set<MapperKey> getUsedMappers() {
        return usedMappers;
    }
    
    public String toString() {
        return getClass().getSimpleName() + "([A]:" + aType + ", [B]:"+bType+")";
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
        final ClassMap<?, ?> other = (ClassMap<?, ?>) obj;
        if (aType == null) {
            if (other.aType != null) {
                return false;
            }
        } else if (!aType.equals(other.aType)) {
            return false;
        }
        if (bType == null) {
            if (other.bType != null) {
                return false;
            }
        } else if (!bType.equals(other.bType)) {
            return false;
        }
        return true;
    }
    
}
