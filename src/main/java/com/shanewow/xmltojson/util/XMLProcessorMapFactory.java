package com.shanewow.xmltojson.util;

import com.shanewow.xmltojson.model.schema.SchemaItem;
import com.shanewow.xmltojson.model.schema.SchemaRoot;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class XMLProcessorMapFactory {

    private interface Processor {
        void process(
                SchemaItem item,
                String location,
                Map<String, AtomicInteger> arrayLocationToIndexCounter,
                Map<String, XMLProcessor.LocationProcessor> context,
                Map<String, String> dataCollector
        ) throws XMLStreamException;
    }

    private static final Map<SchemaItem.TYPE, Processor> PROCESSOR_MAP = Collections.unmodifiableMap(Map.of(
            SchemaItem.TYPE.array, XMLProcessorMapFactory::prepareArray,
            SchemaItem.TYPE.object, XMLProcessorMapFactory::prepareObject,
            SchemaItem.TYPE.integer, XMLProcessorMapFactory::preparePrimitive,
            SchemaItem.TYPE.number, XMLProcessorMapFactory::preparePrimitive,
            SchemaItem.TYPE.string, XMLProcessorMapFactory::preparePrimitive
    ));

    public static Map<String, XMLProcessor.LocationProcessor> createProcessorMap(SchemaRoot schemaRoot, Map<String, String> dataCollector) throws XMLStreamException {
        final Map<String, XMLProcessor.LocationProcessor> context = new HashMap<>();
        final Map<String, AtomicInteger> arrayLocationToIndexCounter = new HashMap<>();

        prepareItem(schemaRoot, "$", arrayLocationToIndexCounter, context, dataCollector);
        return context;
    }

    private static final void prepareItem(SchemaItem item, String location, Map<String, AtomicInteger> arrayLocationToIndexCounter,  Map<String, XMLProcessor.LocationProcessor> processorMap, Map<String, String> dataCollector) throws XMLStreamException {
        if(!PROCESSOR_MAP.containsKey(item.getType())){
            throw new RuntimeException(String.format("Could not find type of %s", item.getType()));
        }
        PROCESSOR_MAP.get(item.getType()).process(item, location, arrayLocationToIndexCounter, processorMap, dataCollector);

        //if parent location is array AND xmlSourcePathIsSet then increase index counter
        if(location.endsWith("[*]") && Objects.nonNull(item.getXmlSourcePath())){
            processorMap.put(item.getXmlSourcePath(), (xmlReader, matchableLocation, actualLocation) -> {
                final long index = arrayLocationToIndexCounter.computeIfAbsent(location, XMLProcessorMapFactory::createNewAtomicInteger).incrementAndGet();
                dataCollector.put(String.format("%s.$length", location), Long.toString(index));
                return false;
            });
        }
    }

    private static final void prepareArray(SchemaItem item, String location, Map<String, AtomicInteger> arrayLocationToIndexCounter,  Map<String, XMLProcessor.LocationProcessor> processorMap, Map<String, String> dataCollector) throws XMLStreamException {
        prepareItem(item.getItems(), String.format("%s[*]", location), arrayLocationToIndexCounter, processorMap, dataCollector);
    }

    private static final void prepareObject(SchemaItem item, String location, Map<String, AtomicInteger> arrayLocationToIndexCounter,  Map<String, XMLProcessor.LocationProcessor> processorMap, Map<String, String> dataCollector)  throws XMLStreamException {
        for(Map.Entry<String, SchemaItem> entry : item.getProperties().entrySet()){
            prepareItem(entry.getValue(), String.format("%s.%s",location, entry.getKey()), arrayLocationToIndexCounter, processorMap, dataCollector);
        }
    }

    private static final void preparePrimitive(SchemaItem item, String location, Map<String, AtomicInteger> arrayLocationToIndexCounter,  Map<String, XMLProcessor.LocationProcessor> processorMap, Map<String, String> dataCollector){
        if(Objects.nonNull(item.getXmlSourcePath())){
            //use xml path to trigger lambda, but store data using parent location
            processorMap.put(item.getXmlSourcePath(), (xmlReader, matchableXmlLocation, actualXmlLocation) -> {
                final String actualLocationWithIndexes = formatActualLocation(location, dataCollector);
                if(Objects.nonNull(item.getXmlFormatter())){
                    dataCollector.put(actualLocationWithIndexes, FormatterFactory.getFormatter(item.getXmlFormatter()).apply(xmlReader.getElementText()));
                }else{
                    dataCollector.put(actualLocationWithIndexes, xmlReader.getElementText());
                }

                return true;
            });
        }
    }


    private static String formatActualLocation(String location, Map<String, String> dataCollector){

        //remove property from current location to get parent
        final String parentLocation = location.substring(0, location.lastIndexOf("."));
//
        //get current length of array
        final Long currentParentLength = Long.parseLong(dataCollector.getOrDefault(String.format("%s.$length", parentLocation), "0"));

        final String toReplace = "*";
        final int start = parentLocation.lastIndexOf(toReplace);

        return new StringBuilder()
            .append(location.substring(0, start))
            .append(currentParentLength - 1)
            .append(location.substring(start + toReplace.length()))
            .toString();

    }

    private static final AtomicInteger createNewAtomicInteger(String key){
        return new AtomicInteger();
    }

}
