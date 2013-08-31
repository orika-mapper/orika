package ma.glasnost.orika.metadata;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * CaseInsensitiveClassMapBuilder is an extension of ClassMapBuilder which performs
 * case-insensitive matching of property names in the 'byDefault()' method.
 * 
 * @author mattdeboer
 *
 * @param <A>
 * @param <B>
 */
public class CaseInsensitiveClassMapBuilder<A,B> extends ClassMapBuilder<A,B> {

    /**
     *
     */
    public static class Factory extends ClassMapBuilderFactory {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.property.PropertyResolverStrategy, ma.glasnost.orika.DefaultFieldMapper[])
         */
        @Override
        protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(
                Type<A> aType, Type<B> bType,
                MapperFactory mapperFactory,
                PropertyResolverStrategy propertyResolver,
                DefaultFieldMapper[] defaults) {
            
            return new CaseInsensitiveClassMapBuilder<A,B>(aType, bType, mapperFactory, propertyResolver, defaults);
        }
    }

    private Map<String, String> lowercasePropertiesForA;
    private Map<String, String> lowercasePropertiesForB;
    private boolean initialized;
    
    /**
     * @param aType
     * @param bType
     * @param mapperFactory
     * @param propertyResolver
     * @param defaults
     */
    protected CaseInsensitiveClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
            PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults) {
        super(aType, bType, mapperFactory, propertyResolver, defaults);
        
        lowercasePropertiesForA = new LinkedHashMap<String, String>();
        for (String prop: this.getPropertyExpressions(getAType()).keySet()) {
            lowercasePropertiesForA.put(prop.toLowerCase(), prop);
        }
        
        lowercasePropertiesForB = new LinkedHashMap<String, String>();
        for (String prop: this.getPropertyExpressions(getBType()).keySet()) {
            lowercasePropertiesForB.put(prop.toLowerCase(), prop);
        }
        initialized = true;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.metadata.ClassMapBuilder#byDefault(ma.glasnost.orika.DefaultFieldMapper[])
     * 
     * Applies default mapping allowing for case-insensitive matching of property names
     * 
     */
    @Override
    public ClassMapBuilder<A, B> byDefault(MappingDirection direction, DefaultFieldMapper... withDefaults) {
        
        super.byDefault(direction, withDefaults);
        
        DefaultFieldMapper[] defaults;
        if (withDefaults.length == 0) {
            defaults = getDefaultFieldMappers();
        } else {
            defaults = withDefaults;
        }
        
        for (final Entry<String, String> entry : lowercasePropertiesForA.entrySet()) {
            String propertyNameA = entry.getValue();
            String lowercaseName = entry.getKey();
            if (!getMappedPropertiesForTypeA().contains(propertyNameA)) {
                if (lowercasePropertiesForB.containsKey(lowercaseName)) {
                    String propertyNameB = lowercasePropertiesForB.get(lowercaseName);
                    if (!getMappedPropertiesForTypeB().contains(propertyNameB)) {
                        /*
                         * Don't include the default mapping of Class to Class; this
                         * property is resolved for all types, but can't be mapped 
                         * in either direction.
                         */
                        if (!propertyNameA.equals("class")) {
                            fieldMap(propertyNameA, propertyNameB, true).direction(direction).add();
                        }
                    }
                } else {
                    Property prop = resolvePropertyForA(propertyNameA);
                    for (DefaultFieldMapper defaulter : defaults) {
                        String suggestion = defaulter.suggestMappedField(propertyNameA, prop.getType());
                        if (suggestion != null && getPropertiesForTypeB().contains(suggestion)) {
                            if (!getMappedPropertiesForTypeB().contains(suggestion)) {
                                fieldMap(propertyNameA, suggestion, true).direction(direction).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * Resolves a property for the particular type, based on the provided property expression
     * 
     * @param type the type to resolve
     * @param expr the property expression to resolve
     * @return the Property referenced by the provided expression
     */
    protected Property resolveProperty(java.lang.reflect.Type type, String expr) {
        String expression = expr;
        if (initialized) {
            Map<String, String> lowercaseProps = type.equals(getAType()) ? 
                    lowercasePropertiesForA : lowercasePropertiesForB;
            String resolvedExpression = lowercaseProps.get(expr.toLowerCase());
            if (resolvedExpression != null) {
                expression = resolvedExpression;
            }
        } 
        return super.resolveProperty(type, expression);
    }
    
}
