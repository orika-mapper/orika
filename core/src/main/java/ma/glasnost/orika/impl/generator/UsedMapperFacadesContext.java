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

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;

/**
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class UsedMapperFacadesContext {
    
    public static class UsedMapperFacadesIndex {
        public Integer index;
        public boolean isReversed;
    }
    
    private List<BoundMapperFacade<Object,Object>> usedMapperFacades = new ArrayList<BoundMapperFacade<Object,Object>>();
    private int usedTypeIndex = 0;
    
    /**
     * Returns an index within the dedicated mapper facades contained in this context
     * for the given pair of types. <br>
     * If the mapping direction is reversed, a negative value is returned, where the absolute
     * value still corresponds to the correct index.<br>
     * 
     * @param sourceType
     * @param destinationType
     * @return
     */
    @SuppressWarnings("unchecked")
    public UsedMapperFacadesIndex getIndex(Type<?> sourceType, Type<?> destinationType, MapperFactory mapperFactory) {
        if (sourceType == null || destinationType == null) {
            throw new NullPointerException("sourceType and destinationType must not be null");
        }
        UsedMapperFacadesIndex result = new UsedMapperFacadesIndex();
        for (int i=0, len = usedMapperFacades.size(); i < len; ++i) {
            BoundMapperFacade<Object,Object> dedicatedFacade = usedMapperFacades.get(i);
            if (dedicatedFacade.getAType().equals(sourceType) && dedicatedFacade.getBType().equals(destinationType)) {
                result.index = i;
                break;
            } else if (dedicatedFacade.getAType().equals(destinationType) && dedicatedFacade.getBType().equals(sourceType)) {
                result.index = i;
                result.isReversed = true;
                break;
            }
        }
        
        if (result.index == null) {
            result.index = Integer.valueOf(usedTypeIndex++);
            usedMapperFacades.add((BoundMapperFacade<Object, Object>)mapperFactory.getMapperFacade(sourceType, destinationType));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public BoundMapperFacade<Object,Object>[] toArray() {
        return usedMapperFacades.toArray(new BoundMapperFacade[0]);
    }
}
