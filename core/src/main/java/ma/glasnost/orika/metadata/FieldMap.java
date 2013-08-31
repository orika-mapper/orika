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

import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.impl.Specifications.Specification;

public class FieldMap implements MappedTypePair<Object, Object> {
    
    private final Property source;
    private final Property destination;
    private final Property aInverse;
    private final Property bInverse;
    private final MappingDirection mappingDirection;
    private final boolean excluded;
    private final String converterId;
    private final boolean byDefault;
    private final String sourceExpression;
    private final String destinationExpression;
    private final Boolean sourceMappedOnNull;
    private final Boolean destinationMappedOnNull;
    
    public FieldMap(Property a, Property b, Property aInverse, Property bInverse, MappingDirection mappingDirection,
            boolean excluded, String converterId, boolean byDefault, Boolean sourceMappedOnNull, Boolean destinationMappedOnNull) {
        this.source = a;
        this.destination = b;
        this.aInverse = aInverse;
        this.bInverse = bInverse;
        this.mappingDirection = mappingDirection;
        this.converterId = converterId;
        this.excluded = excluded;
        this.byDefault = byDefault;
        this.sourceMappedOnNull = sourceMappedOnNull;
        this.destinationMappedOnNull = destinationMappedOnNull;
        this.sourceExpression = this.source.getExpression();
        this.destinationExpression = this.destination.getExpression();
    }
    
    public FieldMap copy() {
        
        return new FieldMap(copy(source), copy(destination), copy(aInverse), copy(bInverse), 
        		mappingDirection, excluded, converterId, byDefault, sourceMappedOnNull, destinationMappedOnNull);
    }
    
    private Property copy(Property property) {
        return property != null ? property.copy() : null;
    }
    
    public Property getSource() {
        return source;
    }
    
    public Property getDestination() {
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public Type<Object> getAType() {
        return (Type<Object>) getSource().getType();
    }

    @SuppressWarnings("unchecked")
    public Type<Object> getBType() {
        return (Type<Object>) getDestination().getType();
    }
    
    String getSourceName() {
        return source.getExpression();
    }
    
    String getDestinationName() {
        return destination.getExpression();
    }
    
    public String getSourceExpression() {
        return sourceExpression;
    }
    
    public String getDestinationExpression() {
        return destinationExpression;
    }
    
    /**
     * @return the sourceMappedOnNull; can be null, which indicates no preference and
     * that the global value should be used.
     */
    public Boolean isSourceMappedOnNull() {
        return sourceMappedOnNull;
    }

    /**
     * @return destinationMappedOnNull; can be null, which indicates no preference and
     * that the global value should be used.
     */
    public Boolean isDestinationMappedOnNull() {
        return destinationMappedOnNull;
    }

    public Property getInverse() {
        return bInverse;
    }
    
    public boolean isIgnored() {
        return MappingDirection.B_TO_A == mappingDirection;
    }
    
    public FieldMap flip() {
        return new FieldMap(destination, source, bInverse, aInverse, mappingDirection.flip(), excluded, converterId, 
        		byDefault, destinationMappedOnNull, sourceMappedOnNull);
    }
    
    public boolean is(Specification specification) {
        return specification.apply(this);
    }
    
    public boolean have(Specification specification) {
        return specification.apply(this);
    }
    
    public String getConverterId() {
        return converterId;
    }
    
    public boolean isByDefault() {
    	return byDefault;
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    
    @Override
    public String toString() {
        return "FieldMap [destination=" + getDestinationExpression() + ", source=" + getSourceExpression() + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aInverse == null) ? 0 : aInverse.hashCode());
		result = prime * result
				+ ((bInverse == null) ? 0 : bInverse.hashCode());
		result = prime * result
				+ ((converterId == null) ? 0 : converterId.hashCode());
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + (excluded ? 1231 : 1237);
		result = prime
				* result
				+ ((mappingDirection == null) ? 0 : mappingDirection.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		FieldMap other = (FieldMap) obj;
		if (aInverse == null) {
			if (other.aInverse != null) {
				return false;
			}
		} else if (!aInverse.equals(other.aInverse)) {
			return false;
		}
		if (bInverse == null) {
			if (other.bInverse != null) {
				return false;
			}
		} else if (!bInverse.equals(other.bInverse)) {
			return false;
		}
		if (converterId == null) {
			if (other.converterId != null) {
				return false;
			}
		} else if (!converterId.equals(other.converterId)) {
			return false;
		}
		if (destination == null) {
			if (other.destination != null) {
				return false;
			}
		} else if (!destination.equals(other.destination)) {
			return false;
		}
		if (excluded != other.excluded) {
			return false;
		}
		if (mappingDirection != other.mappingDirection) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		return true;
	}
    
}
