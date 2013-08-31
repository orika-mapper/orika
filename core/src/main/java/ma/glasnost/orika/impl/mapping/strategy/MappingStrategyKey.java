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

package ma.glasnost.orika.impl.mapping.strategy;

/**
 * MappingStrategyKey defines the minimum information necessary to cache a
 * particular mapping strategy
 * 
 * @author matt.deboer@gmail.com
 *
 */
public final class MappingStrategyKey {
    
	private final Class<?> rawSourceType;
	private final java.lang.reflect.Type sourceType;
	private final java.lang.reflect.Type destinationType;
	private final boolean destinationProvided;
    private final int hashCode;
    
    /**
     * Constructs a new instance of MappingStrategyKey
     * 
     * @param rawSourceType
     * @param sourceType
     * @param destinationType
     * @param destinationProvided
     */
    public MappingStrategyKey(Class<?> rawSourceType, java.lang.reflect.Type sourceType, java.lang.reflect.Type destinationType, boolean destinationProvided) {
        this.rawSourceType = rawSourceType;
        this.sourceType = sourceType;
        this.destinationType = destinationType;
        this.destinationProvided = destinationProvided;
        this.hashCode = computeHashCode();
    }
    
    /**
     * @return the raw source class of the associated mapping strategy
     */
    protected Class<?> getRawSourceType() {
        return rawSourceType;
    }

    /**
     * @return the source type of the associated mapping strategy
     */
    protected java.lang.reflect.Type getSourceType() {
        return sourceType;
    }

    /**
     * @return the destination type of the associated mapping strategy
     */
    protected java.lang.reflect.Type getDestinationType() {
        return destinationType;
    }
    
    /**
     * @return true if the destination is provided
     */
    protected boolean isDestinationProvided() {
    	return destinationProvided;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (destinationProvided ? 1231 : 1237);
        result = prime * result + ((destinationType == null) ? 0 : destinationType.hashCode());
        result = prime * result + ((rawSourceType == null) ? 0 : rawSourceType.hashCode());
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MappingStrategyKey other = (MappingStrategyKey) obj;
        if (destinationProvided != other.destinationProvided)
        	return false;
        if (getDestinationType() == null) {
            if (other.destinationType != null)
                return false;
        } else if (!destinationType.equals(other.destinationType))
            return false;
        if (getRawSourceType() == null) {
            if (other.rawSourceType != null)
                return false;
        } else if (!rawSourceType.equals(other.rawSourceType))
            return false;
        if (getSourceType() == null) {
            if (other.sourceType != null)
                return false;
        } else if (!sourceType.equals(other.sourceType))
            return false;
        
        
        return true;
    }
    
    public String toString() {
    	return "[" + rawSourceType.getSimpleName() + ", " + sourceType + ", " + destinationType +"]";
    }
}
