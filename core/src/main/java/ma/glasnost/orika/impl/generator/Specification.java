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

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * Specification encapsulates the logic to generate code for mapping
 * and comparing a pair of types
 * 
 * @author mattdeboer
 *
 */
public interface Specification extends BaseSpecification {
    

    
    /**
     * Generates code for a boolean equality test between the two variable types,
     * where are potentially unrelated.
     * 
     * @param source
     * @param destination
     * @param code
     * @return the code snippet which represents a true|false statement describing
     * whether the two types should be considered 'equal'
     */
    String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code);
    
    
    /**
     * Generates code to map the provided field map
     * 
     * @param fieldMap the fieldMap for which source code should be generated
     * @param source a convenience wrapper around the source field which can be used facilitate code generation
     * @param destination a convenience wrapper around the destination field which can be used facilitate code generation
     * @param code
     * @return the code snippet which represents mapping from the source to destination
     * property
     */
    String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code);
}
