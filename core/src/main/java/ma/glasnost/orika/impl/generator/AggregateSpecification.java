package ma.glasnost.orika.impl.generator;

import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.FieldMap;

/**
 * AggregateSpecification encapsulates the logic to generate code for mappings
 * which are applied to collections of FieldMaps together
 * 
 * @author mattdeboer
 *
 */
public interface AggregateSpecification {
    
    void setMapperFactory(MapperFactory mapperFactory);
    
    /**
     * Tests whether this Specification applies to the specified MappedTypePair
     * @param fieldMap 
     * 
     * @param typePair
     * @return true if this specification applies to the given MappedTypePair
     */
    boolean appliesTo(FieldMap fieldMap);
    
    
    /**
     * @param fieldMappings
     * @param code
     * @return
     */
    String generateMappingCode(List<FieldMap> fieldMappings, SourceCodeContext code);
}
