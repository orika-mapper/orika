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

package ma.glasnost.orika.unenhance;

import ma.glasnost.orika.metadata.Type;

/**
 * Defines a strategy to unenhance an object specifically a proxy one like those
 * of Hibernate.<br>
 * Unenhanement can be used in cases where a class needs to be "unwrapped" from
 * a proxy, or when a super-type or interface should be used for an object
 * instead of the object's own class.
 * 
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface UnenhanceStrategy {
    
    /**
     * Should return the unenhanced type to be used when determining attribute
     * mapping information for the type.
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T> Type<T> unenhanceType(T object, Type<T> type);
    
    public <T> T unenhanceObject(T object, Type<T> type);
}
