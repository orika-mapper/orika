package ma.glasnost.orika.loader;

import ma.glasnost.orika.loader.nodetypes.IElement;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public interface IParser {
    IParser startElement(XMLEvent event);
    IParser character(XMLEvent event);
    IParser endElement(XMLEvent event);
    IElement detectElement(XMLEvent event);
}
