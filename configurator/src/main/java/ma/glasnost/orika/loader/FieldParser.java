package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.nodetypes.EField;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class FieldParser extends DefaultParser<EField> {
    private ClassMapBuilder classMapBuilder;

    public FieldParser(Class<EField> clazz, MapperFactory factory, IParser parent) {
        super(clazz, factory);
        this.parent = parent;
    }

    public IParser init(ClassMapBuilder classMapBuilder) {
        this.classMapBuilder = classMapBuilder;
        return super.init();
    }

    @Override
    public IParser endElement(XMLEvent event) {
        detectElement(event);
        if (getLastParsedNode().isEvaluated()) {
            testEvaluated();
            classMapBuilder.field(
                enumMap.get(EField.A),
                enumMap.get(EField.B)
            );
            enumMap.clear();
        }
        return super.endElement(event);
    }
}
