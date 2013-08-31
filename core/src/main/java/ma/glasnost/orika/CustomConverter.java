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

import java.lang.reflect.ParameterizedType;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * CustomConverterBase provides a utility base for creating customized converters,
 * which determines type parameters automatically. <br><br>
 * 
 * It is recommend to extend this class to create your own custom converters.
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <S> the source type
 * @param <D> the destination type
 */
public abstract class CustomConverter<S, D> implements ma.glasnost.orika.Converter<S, D> {
    
    protected final Type<S> sourceType;
    protected final Type<D> destinationType;
    protected MapperFacade mapperFacade;
    
    public CustomConverter() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType)genericSuperclass;
            sourceType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
            destinationType = TypeFactory.valueOf(superType.getActualTypeArguments()[1]);
        } else {
            throw new IllegalStateException("When you subclass the ConverterBase S and D type-parameters are required.");
        }
    }
    
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return this.sourceType.isAssignableFrom(sourceType) && this.destinationType.equals(destinationType);
    }
    
    public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }
    
    public String toString() {
    	String subClass = getClass().equals(CustomConverter.class) ? "" : "("+getClass().getSimpleName()+")";
    	return CustomConverter.class.getSimpleName()+subClass+"<"+sourceType + ", " + destinationType+">";
    }
    
    public Type<S> getAType() {
        return sourceType;
    }
    
    public Type<D> getBType() {
        return destinationType;
    }
}
