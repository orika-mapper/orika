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

package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * AbstractSpecification provides the base implementation for Specification
 */
public abstract class AbstractSpecification implements Specification {
    
    /**
     * 
     */
    protected MapperFactory mapperFactory;
    
    public void setMapperFactory(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
    
    /**
     * Tests whether this fieldMap should map nulls;
     * 
     * @param fieldMap
     * @param context
     * @return true if nulls should be mapped for this FeildMap
     */
    public static boolean shouldMapNulls(FieldMap fieldMap, SourceCodeContext context) {
        Boolean mapNull = fieldMap.isDestinationMappedOnNull();
        if (mapNull == null) {
            mapNull = context.shouldMapNulls();
        }
        return mapNull;
    }
    
    public abstract boolean appliesTo(FieldMap fieldMap);
    
    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return format("%s.equals(%s)", source, destination);
    }
    
    public abstract String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code);
    
}
