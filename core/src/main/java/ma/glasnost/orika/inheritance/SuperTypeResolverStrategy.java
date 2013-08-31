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

package ma.glasnost.orika.inheritance;

import ma.glasnost.orika.metadata.Type;

/**
 * This strategy is used by the super-type resolver to 
 * determine when and how to lookup a super-type, and and
 * also provides decision as to whether the super-type is
 * accepted.
 * 
 * @author matt.deboer@gmail.com
 */
public interface SuperTypeResolverStrategy {
	
    /**
     * @param type
     * @return true if the proposed super-type is acceptable
     */
    public boolean accept(Type<?> type);
    
    /**
     * 
     * @param type
     * @return true if a super-type should be looked up for the proposed type; 
     * false signifies that the class should be returned as-is.
     */
    public boolean shouldLookupSuperType(Type<?> type);
    
    /**
     * @return true if a super class(es) should be looked-up first before
     * trying interfaces
     */
    public boolean shouldPreferClassOverInterface();
}
