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
package ma.glasnost.orika.constructor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Type;

/**
 * ConstructorResolverStrategy defines a contract for resolving
 * a constructor which may be used to construct a new instance of
 * a type from a particular ClassMap definition.
 *
 */
public interface ConstructorResolverStrategy {
    
    <T, A, B> ConstructorMapping<T> resolve(ClassMap<A, B> classMap, Type<T> sourceType);
    
    /**
     * ConstructorMapping represents the results of resolving a constructor
     * from type <code>T</code> against a given ClassMap.<br><br>
     * 
     * 
     * @param <T>
     */
    public static class ConstructorMapping<T> {
    	
    	private Constructor<T> constructor;
    	private List<FieldMap> mappedFields;
    	private boolean parameterNameInfoAvailable;
    	private String[] declaredParameters;
    	private Type<?>[] parameterTypes;
    	
		/**
         * @return the parameterTypes
         */
        public Type<?>[] getParameterTypes() {
            return parameterTypes;
        }
        /**
         * @param parameterTypes the parameterTypes to set
         */
        public void setParameterTypes(Type<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }
        public boolean isParameterNameInfoAvailable() {
			return parameterNameInfoAvailable;
		}
		public void setParameterNameInfoAvailable(boolean parameterNameInfoAvailable) {
			this.parameterNameInfoAvailable = parameterNameInfoAvailable;
		}
		public String[] getDeclaredParameters() {
			return declaredParameters;
		}
		public void setDeclaredParameters(String[] declaredParameters) {
			this.declaredParameters = declaredParameters;
		}
		public Constructor<T> getConstructor() {
			return constructor;
		}
		public void setConstructor(Constructor<T> constructor) {
			this.constructor = constructor;
		}
		public List<FieldMap> getMappedFields() {
			if (mappedFields == null) {
				mappedFields = new ArrayList<FieldMap>();
			}
			return mappedFields;
		}

    }
}
