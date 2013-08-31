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

/**
 * MapKeyProperty is a special Property instance used to represent a value
 * which associated with a key within a Map.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MapKeyProperty extends Property {
    
    public MapKeyProperty(String key, Type<?> type, Property owner) {
        super(key,key,"get(\"" + key + "\")","put(\"" + key + "\",%s)",type,null, owner);
    }
    
    public boolean isMapKey() {
        return true;
    }
}
