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

import java.util.Map;

/**
 * @author matt.deboer@gmail.com
 *
 */
public interface MappingContextFactory {
    
    /**
     * Gets an available instance of MappingContext
     * 
     * @return an instance of MappingContext
     */
    public MappingContext getContext();
    
    /**
     * Allows for implementations that reuse objects to clean-up/clear any resources
     * associated with the particular context instance once it is no longer needed.
     * 
     * @param context the context to be recycled
     */
    public void release(MappingContext context);
    
    
    /**
     * @return a reference to the global properties map for this mapping context factory; any properties
     * set here are available from any individual MappingContext created by this factory.
     */
    public Map<Object, Object> getGlobalProperties();
}
