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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @param <S>
 * @param <D>
 * 
 * @deprecated use {@link ma.glasnost.orika.CustomConverter} instead
 */
@Deprecated
public abstract class TypeConverter<S, D> extends CustomConverterBase<S, D> {
    
    protected final Class<S> sourceClass;
    protected final Class<D> destinationClass;
    
    @SuppressWarnings("unchecked")
    public TypeConverter() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            sourceClass = (Class<S>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
            destinationClass = (Class<D>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[1];
        } else {
            throw new IllegalStateException("When you subclass the TypeConverter S and D type-parameters are required.");
        }
    }
    
    public boolean canConvert(Class<S> sourceClass, Class<? extends D> destinationClass) {
        return this.sourceClass.equals(sourceClass) && this.destinationClass.equals(destinationClass);
    }
}
