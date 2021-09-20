/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package ma.glasnost.orika.test;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MappingUtil {
    
    public static final String DISABLE_DEBUG_MODE = "ma.glasnost.orika.test.MappingUtil.noDebug";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingUtil.class);
    
    /**
     * @return a new default instance of MapperFactory
     */
    public static MapperFactory getMapperFactory() {
        return new DefaultMapperFactory.Builder().build();
    }
    
    /**
     * @return a new default instance of MapperFactory, with the specified debug
     * mode configuration.
     * @param debugMode if true, EclipseJdt will be used for the compiler
     * strategy (for step-debugging in IDEs), and class and source files will be written to disk.
     */
    public static MapperFactory getMapperFactory(boolean debugMode) {
        if (debugMode) {
            if (Boolean.parseBoolean(System.getProperty(DISABLE_DEBUG_MODE))) {
                LOGGER.warn("Debug mode was requested via MappingUtil when it was explicitly disabled");
                return getMapperFactory();
            } else {
                return new DefaultMapperFactory.Builder().build();
            }
        } else {
            return getMapperFactory();
        }
    }
}
