/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2013 Orika authors
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
 * A filter that does no filtering. This provides a useful base class for simple filters
 * that just want to implement one facet.
 * 
 * @param <A>
 * @param <B>
 */
public class NullFilter<A, B> extends CustomFilter<A, B> {
    /**
     * @return false
     */
    @Override
    public boolean filtersSource() {
        return false;
    }

    /**
     * @return false
     */
    @Override
    public boolean filtersDestination() {
        return false;
    }

    /**
     * @return true
     */
    @Override
    public <S extends A, D extends B> boolean shouldMap(final Type<S> sourceType, final String sourceName, final S source, final Type<D> destType, final String destName,
            final MappingContext mappingContext) {
        return true;
    }

    /**
     * @return destinationValue
     */
    @Override
    public <D extends B> D filterDestination(D destinationValue, final Type<?> sourceType, final String sourceName, final Type<D> destType,
            final String destName, final MappingContext mappingContext) {
        return destinationValue;
    }

    /**
     * @return sourceValue
     */
    @Override
    public <S extends A> S filterSource(final S sourceValue, final Type<S> sourceType, final String sourceName, final Type<?> destType,
            final String destName, final MappingContext mappingContext) {
        return sourceValue;
    }
}
