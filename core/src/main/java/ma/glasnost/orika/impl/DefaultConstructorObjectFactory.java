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

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;

/**
 * DefaultConstructorObjectFactory is used for types which should be instantiated
 * using their default constructor.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class DefaultConstructorObjectFactory<T> implements ObjectFactory<T> {

    private final Class<T> type;
    
    public DefaultConstructorObjectFactory(Class<T> type) {
        this.type = type;
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.ObjectFactory#create(java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public T create(Object source, MappingContext mappingContext) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    
}
