/*
 * Orika - simpler, better and faster Java bean mapping
 *
 *  Copyright (C) 2011-2019 Sidi Mohamed EL AATIFI <elaatifi@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ma.glasnost.orika;

import ma.glasnost.orika.metadata.Type;

public abstract class BaseConverter<S,D> implements Converter<S,D> {

    private final Type<S> sourceType;
    private final Type<D> destinationType;
    private MapperFacade mapperFacade;

    protected BaseConverter(Type<S> sourceType, Type<D> destinationType) {
        this.sourceType = sourceType;
        this.destinationType = destinationType;

    }

    @Override public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }

    @Override public Type<S> getAType() {
        return sourceType;
    }

    @Override public Type<D> getBType() {
        return destinationType;
    }
}
