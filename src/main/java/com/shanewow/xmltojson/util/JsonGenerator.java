package com.shanewow.xmltojson.util;

import com.google.gson.stream.JsonWriter;
import com.shanewow.xmltojson.model.schema.SchemaItem;
import com.shanewow.xmltojson.model.schema.SchemaRoot;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.stream.LongStream;

public class JsonGenerator {

    private interface Processor {
        void process(
                SchemaItem item,
                String location,
                Map<String, String> data,
                JsonWriter jsonWriter
        ) throws IOException;
    }

    private static final Map<SchemaItem.TYPE, JsonGenerator.Processor> PROCESSOR_MAP = Collections.unmodifiableMap(Map.of(
            SchemaItem.TYPE.array, JsonGenerator::writeArray,
            SchemaItem.TYPE.object, JsonGenerator::writeObject,
            SchemaItem.TYPE.integer, JsonGenerator::writeInteger,
            SchemaItem.TYPE.number, JsonGenerator::writeNumber,
            SchemaItem.TYPE.string, JsonGenerator::writeString
    ));



    public static void toJson(SchemaRoot schemaRoot, Map<String, String> data, Writer writer) throws IOException {
        final String location = "$";
        try(final JsonWriter jsonWriter = new JsonWriter(writer)){
            jsonWriter.setIndent("  ");
            writeItem(schemaRoot, location, Collections.unmodifiableMap(data), jsonWriter);
        }
    }





    private static final void writeItem(SchemaItem item, String location, Map<String, String> data, JsonWriter jsonWriter) throws IOException {
        if(!PROCESSOR_MAP.containsKey(item.getType())){
            throw new RuntimeException(String.format("Could not find type of %s", item.getType()));
        }
        PROCESSOR_MAP.get(item.getType()).process(item, location, data, jsonWriter);
    }

    private static final void writeArray(SchemaItem item, String location, Map<String, String> data, JsonWriter jsonWriter) throws IOException {
        jsonWriter.beginArray();
        LongStream.range(0, getCount(data, String.format("%s[*]", location)))
                .forEach(index -> {
                    try {
                        writeItem(item.getItems(), String.format("%s[%s]", location, index), data, jsonWriter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        jsonWriter.endArray();
    }

    private static final void writeObject(SchemaItem item, String location, Map<String, String> data, JsonWriter jsonWriter)  throws IOException {
        jsonWriter.beginObject();
        for(Map.Entry<String, SchemaItem> entry : item.getProperties().entrySet()){
            jsonWriter.name(entry.getKey());
            writeItem(entry.getValue(), String.format("%s.%s", location, entry.getKey()), data, jsonWriter);
        }
        jsonWriter.endObject();
    }

    private static final void writeInteger(SchemaItem item, String location , Map<String, String> data, JsonWriter jsonWriter)  throws IOException {
        jsonWriter.value(new BigInteger(data.get(location)));
    }

    private static final void writeString(SchemaItem item, String location, Map<String, String> data, JsonWriter jsonWriter)  throws IOException {
        jsonWriter.value(data.get(location));
    }

    private static final void writeNumber(SchemaItem item, String location, Map<String, String> data, JsonWriter jsonWriter)  throws IOException {
        jsonWriter.value(new BigDecimal(data.get(location)));
    }


    //UTIL
    private static long getCount(Map<String, String> data, String location){
        return Long.parseLong(data.get(String.format("%s.$length", location)));
    }
}
