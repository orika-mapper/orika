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

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

public class MappingException extends RuntimeException {

	private static final long serialVersionUID = -1485137975363692382L;

	private Property sourceProperty;
	private Property destinationProperty;
	private Class<?> sourceClass;
	private Type<?> sourceType;
	private Type<?> destinationType;
	
	public MappingException(Throwable e) {
		super(e);
	}

	public MappingException(String message) {
		super(message);
	}
	
	public MappingException(String message, Throwable e) {
		super(message, e);
	}

	public String getLocalizedMessage() {
		
		StringBuilder message = new StringBuilder();
		if (sourceClass != null) {
			message.append("\nsourceClass = " + sourceClass);
		}
		if (sourceType != null) {
			message.append("\nsourceType = " + sourceType);
		}
		if (sourceProperty != null) {
			message.append("\nsourceProperty = " + sourceProperty);
		}
		if (destinationType != null) {
			message.append("\ndestinationType = " + destinationType);
		}
		if (destinationProperty != null) {
			message.append("\ndestinationProperty = " + destinationProperty);
		}
		if (message.length() > 0) {
			message.insert(0, "While attempting the folling mapping:");
			message.append("\nError occurred: ");
		}
		
		message.append(super.getLocalizedMessage());
		
		return message.toString();
	}
	
	public Property getSourceProperty() {
		return sourceProperty;
	}

	public void setSourceProperty(Property sourceProperty) {
		this.sourceProperty = sourceProperty;
	}

	public Property getDestinationProperty() {
		return destinationProperty;
	}

	public void setDestinationProperty(Property destinationProperty) {
		this.destinationProperty = destinationProperty;
	}

	public Type<?> getSourceType() {
		return sourceType;
	}

	public void setSourceType(Type<?> sourceType) {
		this.sourceType = sourceType;
	}

	public Type<?> getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(Type<?> destinationType) {
		this.destinationType = destinationType;
	}

	public Class<?> getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(Class<?> sourceClass) {
		this.sourceClass = sourceClass;
	}
}
