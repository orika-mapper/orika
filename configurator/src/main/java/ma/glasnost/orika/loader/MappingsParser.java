package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.nodetypes.EMapping;
import ma.glasnost.orika.loader.nodetypes.EMappings;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class MappingsParser extends DefaultParser<EMappings> {

    private static final String MAPPING = "mapping";
    private MappingParser mappingLoader;

    public MappingsParser(Class<EMappings> clazz, MapperFactory factory) {
        super(clazz, factory);
        mappingLoader = new MappingParser(EMapping.class, factory, this);
        parent = this;
    }

    @Override
    public IParser startElement(XMLEvent event) {
        detectElement(event);
        if (lastParsedNode == EMappings.MAPPING) {
            return mappingLoader.init();
        }
        return super.startElement(event);
    }
}
