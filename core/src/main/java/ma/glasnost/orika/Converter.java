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
 * Converter is used to provide direct conversion from one type to another,
 * useful for those scenarios where complete control over the mapping is
 * desired.<br><br>
 * 
 * Note that an instance of the current MapperFacade is provided in the <code>convert</code>
 * method for cases where you only want to control a specific portion of the mapping, 
 * but wish to delegate some or all of the mapping of the nested types.
 * <br><br>
 * 
 * See also {@link ma.glasnost.orika.CustomConverter} for a base class which can be
 * extended to create your own custom converter instance.
 *
 * @param <S>
 * @param <D>
 */
public interface Converter<S, D> extends MappedTypePair<S, D> {
    
    /**
     * Answers whether this converter can be used to handle the conversion of <code>sourceType</code>
     * to <code>destinationType</code>.
     * 
     * @param sourceType
     * @param destinationType
     * @return
     */
    boolean canConvert(Type<?> sourceType, Type<?> destinationType);
    
    /**
     * Perform the conversion of <code>source</code> into a new instance of
     * <code>destinationType</code>.
     * 
     * @param source the source object to be converted
     * @param destinationType the destination type to produce
     * @return a new instance of <code>destinationType</code>
     */
    D convert(S source, Type<? extends D> destinationType);
    
    /**
     * Store an instance of the current MapperFacade which may be used 
     * in mapping of nested types.
     * 
     * @param mapper the current MapperFacade
     */
    void setMapperFacade(MapperFacade mapper);
}
