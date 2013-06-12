package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.nodetypes.EField;
import ma.glasnost.orika.loader.nodetypes.EMapping;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class MappingParser extends DefaultParser<EMapping> {
    private FieldParser fieldLoader;
    private ClassMapBuilder classMapBuilder;

    public MappingParser(Class clazz, MapperFactory factory, IParser parent) {
        super(clazz, factory);
        this.parent = parent;
        fieldLoader = new FieldParser(EField.class, factory, this);
    }

    @Override
    public IParser init() {
        classMapBuilder = null;
        lastParsedNode = EMapping.MAPPING;
        return super.init();
    }

    @Override
    public IParser startElement(XMLEvent event) {
        detectElement(event);
        if (lastParsedNode == EMapping.FIELD) {
            if (classMapBuilder == null) {
                throw new RuntimeException("Ошибка мапинга, не заданы классы для мапинга");
            }
            return fieldLoader;
        }
        return this;
    }

    @Override
    public IParser endElement(XMLEvent event) {
        detectElement(event);
        if (getLastParsedNode().isEvaluated()) {
            testEvaluated();
            try {
                classMapBuilder = factory.classMap(
                    Class.forName(enumMap.get(EMapping.CLASS_A)),
                    Class.forName(enumMap.get(EMapping.CLASS_B))
                    );
                fieldLoader.init(classMapBuilder);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Not found class " +
                    e.getMessage()
                );
            }
            enumMap.clear();
        }
        if (getLastParsedNode().isEnded()) {
            classMapBuilder.byDefault().register();
        }
        return super.endElement(event);
    }
}
