package ma.glasnost.orika.metadata;

import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * NestedElementProperty represents a property which belongs to the type of a nested list/array/map element
 * within another type.
 * 
 * @author mattdeboer
 *
 */
public class NestedElementProperty extends Property {

    private final Property elementProperty;
    
    /**
     * @param owningProperty
     * @param elementProperty
     * @param resolver 
     */
    public NestedElementProperty(Property owningProperty, Property elementProperty, PropertyResolverStrategy resolver) {
        super(getElementExpression(owningProperty, elementProperty), 
                elementProperty.getName(), elementProperty.getGetter(), elementProperty.getSetter(),
                elementProperty.getType(), elementProperty.getElementType(),
                initContainer(owningProperty, elementProperty.getExpression(), resolver)
                );

        Property element =  initElement(getContainer(), elementProperty.getExpression(), resolver);
        if (element == null) {
            element = elementProperty;
        }
        this.elementProperty = element;
    }
    
    /**
     * Returns the element expression for this property
     * 
     * @return the element expression for this property
     */
    private static String getElementExpression(Property owningProperty, Property elementProperty) {
        int splitIndex = owningProperty.getExpression().lastIndexOf("{");
        if (splitIndex >= 0) {
            return owningProperty.getExpression().substring(0, splitIndex + 1) + 
                    "{" + elementProperty.getExpression() + "}" +
                    owningProperty.getExpression().substring(splitIndex + 1);
        } else {
            return owningProperty.getExpression() + "{" + elementProperty.getExpression() + "}";
        }
    }
    
    private static Property initContainer(Property owningProperty, String propertyExpression, PropertyResolverStrategy resolver) {
        String[] parts = propertyExpression.replace("}","").split("\\{");
        if (parts.length > 1) {
            StringBuilder containerExpression = new StringBuilder("");
            for (int i = parts.length -2; i >= 0; --i) {
                String part = parts[i];
                containerExpression.insert(0, "{" + part);
                containerExpression.append("}");
            }
            Property container = "".equals(containerExpression.toString()) ? owningProperty : resolver.getProperty(owningProperty, containerExpression.toString());
            return container;
        } else {
            return owningProperty;
        }
    }
    
    private static Property initElement(Property container, String propertyExpression, PropertyResolverStrategy resolver) {
        String[] parts = propertyExpression.replace("}","").split("\\{");
        if (parts.length > 1) {
            return resolver.getProperty(container, "{" + parts[parts.length-1] + "}");
        } else {
            return null;
        }
    }
  
    /**
     * @return the nested element property 
     */
    public Property getElement() {
        return elementProperty;
    }
    /**
     * @return the root container of this property
     */
    public Property getRootContainer() {
        Property container = getContainer();
        while (container.getContainer() != null) {
            container = container.getContainer();
        }
        return container;
    }
}
