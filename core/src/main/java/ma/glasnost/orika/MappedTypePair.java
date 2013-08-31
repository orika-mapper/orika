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
package ma.glasnost.orika;

import ma.glasnost.orika.metadata.Type;

/**
 * MappedTypePair represents any pair of mapped types, which are distinguished
 * from each other as 'A type' and 'B type'
 * 
 * @author matt.deboer@gmail.com
 *
 */
public interface MappedTypePair<A, B> {
    /**
     * @return the 'A' type for this mapped pair
     */
    Type<A> getAType();
    
    /**
     * @return the 'B' type for this mapped pair
     */
    Type<B> getBType();
}
