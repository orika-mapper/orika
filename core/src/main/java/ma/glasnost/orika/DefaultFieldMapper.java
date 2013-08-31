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
 * Provides a generic mapping suggestion mechanism to provide 
 * help with guessing default mappings of fields when a straight
 * equivalent name match is not found
 * 
 * @author matt.deboer@gmail.com
 */
public interface DefaultFieldMapper {
	
	/**
	 * @param sourceExpression
	 * @return a suggested optional mapping name for the given property,
	 * or <code>null</code> if no suggestion for the given property
	 */
	public String suggestMappedField(String fromProperty, Type<?> fromPropertyType);
	
}
