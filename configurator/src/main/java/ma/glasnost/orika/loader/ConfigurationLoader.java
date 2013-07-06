/*
 * Copyright 2005-2010 the original author or authors.
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
package ma.glasnost.orika.loader;


import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.nodetypes.EMappings;
import ma.glasnost.orika.loader.nodetypes.IElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Stack;

/**
 * Map configure file parser
 */
public class ConfigurationLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);

    private XMLEventAllocator allocator;
    private MapperFactory factory;

    public ConfigurationLoader() {
    }

    public void load(MapperFactory factory, String resourceName) {
        this.factory = factory;
        URL url = getClass().getClassLoader().getResource(resourceName);
        String filename = url.getFile();
        log.debug(resourceName + ", " + url.getPath());
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            log.debug("FACTORY: " + xmlif);
            xmlif.setEventAllocator(new XMLEventAllocatorImpl());
            allocator = xmlif.getEventAllocator();
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(filename,
                    new FileInputStream(filename));

            int eventType = xmlr.getEventType();
            IParser loader = new MappingsParser(EMappings.class, factory);
            Stack<IParser> loaderStack = new Stack<IParser>();
            loaderStack.push(loader);

            while(xmlr.hasNext()) {
                eventType = xmlr.next();
                log.debug(">>>> eventType " + eventType);
                XMLEvent event = allocator.allocate(xmlr);
                if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    log.debug(">>>> END_CONFIGURE");
                    break;
                }

                IElement element = loader.detectElement(event);
                if (element == null) {
                    continue;
                }
                if (element.isIgnored()) {
                    if (eventType == XMLStreamConstants.START_ELEMENT) {
                        log.info("Element " + element.getLocalPart() + " ignored");
                    }
                    continue;
                }

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    loader = loader.startElement(event);
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    loader = loader.endElement(event);
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    loader = loader.character(event);
                } else if (eventType == XMLStreamConstants.COMMENT) {
                } else {
                    log.warn("!!!!!!!!! " + event.toString());
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Configuration file not found");
        } catch (XMLStreamException e) {
            log.error("XMLStreamException");
        }
    }

}
