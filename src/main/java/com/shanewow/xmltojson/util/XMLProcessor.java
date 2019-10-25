package com.shanewow.xmltojson.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class XMLProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLProcessor.class);

    private interface ElementProcessor{
        void process(XMLStreamReader reader) throws XMLStreamException;
    }

    public interface LocationProcessor{
        boolean process(XMLStreamReader reader, String matchableLocation, String actualLocation) throws XMLStreamException;
    }

    private final Map<Integer, ElementProcessor> eventTypeToProcessor;
    private final Map<String, AtomicInteger> elementIndex;
    private final Map<String, LocationProcessor> locationToProcessor;

    private String currentLocation;

    public XMLProcessor(Map<String, LocationProcessor> locationToProcessor){
        LOGGER.info("New XMLProcessor With Keys: {}", locationToProcessor.keySet());
        this.currentLocation = "$";
        this.elementIndex = new HashMap<>();
        this.eventTypeToProcessor = Collections.unmodifiableMap(Map.of(
            XMLStreamReader.START_ELEMENT, this::startElement,
            XMLStreamReader.END_ELEMENT, this::endElement
        ));
        this.locationToProcessor = locationToProcessor;
    }

    public void process(XMLStreamReader xmlReader) throws XMLStreamException {
        while(xmlReader.hasNext()){
            int xmlEvent = xmlReader.next();
            getProcessor(xmlEvent).process(xmlReader);
        }
    }

    private ElementProcessor getProcessor(Integer eventType){
        return eventTypeToProcessor.getOrDefault(eventType, this::defaultConsumer);
    }

    private void defaultConsumer(XMLStreamReader reader){
//        LOGGER.trace("Unknown Event Type");
    }

    private void startElement(XMLStreamReader reader) throws XMLStreamException {

        final String currentLocationWithoutIndex = String.format("%s.%s", currentLocation, reader.getLocalName());
        currentLocation = String.format("%s[%s]", currentLocationWithoutIndex, elementIndex.computeIfAbsent(currentLocationWithoutIndex, XMLProcessor::createIfNull).getAndIncrement());
        final String currentLocationWithWildCards = currentLocation.replaceAll("\\[[0-9]*\\]", "[*]");

        LOGGER.info("Start Element: {}", currentLocation);

        if(processLocation(currentLocationWithoutIndex, currentLocation, reader)){
            endElement(reader);
            return;
        }

        if(processLocation(currentLocation, currentLocation, reader)){
            endElement(reader);
            return;
        }

        if(processLocation(currentLocationWithWildCards, currentLocation, reader)){
            endElement(reader);
            return;
        }
    }

    private void endElement(XMLStreamReader reader){
        currentLocation = currentLocation.indexOf(".") > -1
                ? currentLocation.substring(0, currentLocation.lastIndexOf("."))
                : currentLocation;

        LOGGER.info("End Element: {} New Location: {}", reader.getLocalName(), currentLocation);
    }

    private boolean processLocation(String matchableLocation, String actualLocation, XMLStreamReader reader) throws XMLStreamException {
        if(locationToProcessor.containsKey(matchableLocation)){
            LOGGER.info("Processing Location: {}", matchableLocation);
            final LocationProcessor elementProcessor = locationToProcessor.get(matchableLocation);
            return elementProcessor.process(reader, matchableLocation, actualLocation);
        }
        LOGGER.trace("Skipping Location: {}", matchableLocation);
        return false;

    }

    private static AtomicInteger createIfNull(String key){
        return new AtomicInteger(0);
    }
}
