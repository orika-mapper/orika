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
package ma.glasnost.orika.impl.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ma.glasnost.orika.Filter;

/**
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class UsedFiltersContext {
    
    private Map<Filter<Object, Object>,Integer> usedFilters = new HashMap<Filter<Object, Object>,Integer>();
    private int usedTypeIndex = 0;
    
    /**
     * @param filter
     * @return the index of the specified filter in the array of filters used by 
     * the associated mapping object
     */
    @SuppressWarnings("unchecked")
    public int getIndex(Filter<?, ?> filter) {
        if (filter==null) {
            throw new NullPointerException("type must not be null");
        }
        Integer index = usedFilters.get(filter);
        if (index == null) {
            index = Integer.valueOf(usedTypeIndex++);
            usedFilters.put((Filter<Object, Object>)filter, index);
        }
        return index;
    }
    
    /**
     * @return the array of filters used by the associated mapping object
     */
    public Filter<Object, Object>[] toArray() {
        @SuppressWarnings("unchecked")
        Filter<Object, Object>[] filters = new Filter[usedFilters.size()];
        for (Entry<Filter<Object, Object>, Integer> entry: usedFilters.entrySet()) {
            filters[entry.getValue()] = entry.getKey();
        }
        return filters;
    }
}
