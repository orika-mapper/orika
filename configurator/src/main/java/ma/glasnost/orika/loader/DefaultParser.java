package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.nodetypes.IElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

/**
 * @author  ikozar
 * date     13.02.13
 */
public abstract class DefaultParser<E extends Enum> implements IParser {
    protected static Logger log;

    protected IParser parent;           // Loader for parent level
    protected Class<E> enumNodeNames;   // enum contains levels node names
    protected Enum lastParsedNode;
    protected String lastParsedChars;
    protected XMLEvent lastEvent;
    protected Iterator lastAttIterator; // attribute of last node
    protected EnumMap<String> enumMap;
    protected MapperFactory factory;

    public DefaultParser(Class<E> clazz, MapperFactory factory) {
        log = LoggerFactory.getLogger(this.getClass());
        enumNodeNames = clazz;
        enumMap = new EnumMap<String>(enumNodeNames);
        this.factory = factory;
    }

    /**
     * find type node by localPart
     * @param localPart
     * @return
     */
    protected Enum findLocalPart(String localPart)
    {
        for (Enum enumVal : enumNodeNames.getEnumConstants())
        {
            if (((IElement) enumVal).getLocalPart().equals(localPart))
            {
                return enumVal;
            }
        }
        log.error("Undefined element - " + localPart);
        return null;
    }

    /**
     * detect node type for event
     * @param event
     * @return
     */
    public IElement detectElement(XMLEvent event) {
        if (lastEvent != event) {
            if (event.isStartElement()) {
                lastParsedNode = findLocalPart(event.asStartElement().getName().getLocalPart());
                lastAttIterator = event.asStartElement().getAttributes();
            } else if (event.isEndElement()) {
                lastParsedNode = findLocalPart(event.asEndElement().getName().getLocalPart());
            }
        }
        return getLastParsedNode();
    }

    public IElement getLastParsedNode() {
        return (IElement) lastParsedNode;
    }

    /**
     * Debug print attributes
     */
    protected void printAttributes() {
        for (; lastAttIterator.hasNext(); ) {
            Attribute att = (Attribute) lastAttIterator.next();
            System.out.print(att.getName().getLocalPart() + '=' + att.getValue() + ' ');
        }
        System.out.println("");
    }

    /**
     * init loader
     * @return
     */
    public IParser init() {
        enumMap.clear();
        return this;
    }

    /**
     * handle start element event
     *
     * @param event
     * @return
     */
    public IParser startElement(XMLEvent event) {
        detectElement(event);
        if (getLastParsedNode().isIgnored()) {
            log.info("Element " + getLastParsedNode().getLocalPart() + " ignored");
        }
//        printAttributes();
        return this;
    }

    /**
     * handle charcters event
     *
     * @param event
     * @return
     */
    public IParser character(XMLEvent event) {
        lastParsedChars = event.asCharacters().getData();
        if (!getLastParsedNode().isIgnored() && lastParsedNode != null) {
            enumMap.put(lastParsedNode, lastParsedChars);
        }
        return this;
    }

    /**
     * test for evaluating all needed nodes
     */
    protected void testEvaluated() {
        if (getLastParsedNode().isEvaluated()) {
            for (Enum enumVal : enumNodeNames.getEnumConstants())
            {
                if (((IElement) enumVal).isRequired()
                        && enumMap.get(enumVal) == null)
                    throw new RuntimeException("Skipped required element " + ((IElement) enumVal).getLocalPart());
            }
        }
    }

    /**
     * handle for end element event
     *
     * @param event
     * @return
     */
    public IParser endElement(XMLEvent event) {
        detectElement(event);
        IParser retLoader = getLastParsedNode().isEnded() ? parent : this;
        lastParsedNode = null;
        return retLoader;
    }
}
