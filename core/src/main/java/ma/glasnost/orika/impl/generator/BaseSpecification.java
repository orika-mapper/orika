package ma.glasnost.orika.impl.generator;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * Base Specification that contains the common methods for all Specifications.
 * See {@link Specification} and {@link AggregateSpecification}
 *
 * @author Kalyan Ayyagari kalyan01
 */
public interface BaseSpecification {
    /**
     * Tests whether this Specification applies to the specified MappedTypePair
     * @param fieldMap
     *
     * @return true if this specification applies to the given MappedTypePair
     */
    boolean appliesTo(FieldMap fieldMap);


   void setMapperFactory(MapperFactory mapperFactory);
}
