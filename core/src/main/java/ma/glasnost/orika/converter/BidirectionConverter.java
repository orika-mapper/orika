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
package ma.glasnost.orika.converter;


/**
 * 
 * 
 * @author 
 *
 * @param <S>
 * @param <D>
 * 
 * @deprecated use {@link ma.glasnost.orika.converter.BidirectionalConverter} instead
 */
@Deprecated
public abstract class BidirectionConverter<S, D> extends TypeConverter<Object, Object> implements Converter<Object, Object> {
    
    public abstract D convertTo(S source, Class<D> destinationClass);
    
    public abstract S convertFrom(D source, Class<S> destinationClass);
    
    @SuppressWarnings("unchecked")
    public Object convert(Object source, Class<? extends Object> destinationClass) {
        if (destinationClass.equals(this.destinationClass)) {
            return convertTo((S) source, (Class<D>) destinationClass);
        } else {
            return convertFrom((D) source, (Class<S>) destinationClass);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class<Object> sourceClass, Class<? extends Object> destinationClass) {
        return super.canConvert(sourceClass, destinationClass) || super.canConvert((Class<Object>) destinationClass, sourceClass);
    }
    
}
