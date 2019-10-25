package com.shanewow.xmltojson.command;

import com.google.gson.Gson;
import com.shanewow.xmltojson.model.schema.SchemaRoot;
import com.shanewow.xmltojson.util.JsonGenerator;
import com.shanewow.xmltojson.util.XMLProcessor;
import com.shanewow.xmltojson.util.XMLProcessorMapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ResourceUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@ShellComponent
public class Process {

    private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);

    private final Gson gson;

    public Process(Gson gson) {
        this.gson = gson;
    }

    @ShellMethod("Load and output the parsed schema to the console")
    public String validateSchema(@ShellOption String path) throws IOException {
        final SchemaRoot schemaRoot = gson.fromJson(readFile(path), SchemaRoot.class);
        return gson.toJson(schemaRoot);
    }

    @ShellMethod("Process xml file using the json schema")
    public String processSchema(
            @ShellOption String schemaPath,
            @ShellOption String xmlPath
    ) throws IOException, XMLStreamException {

        //parse json schema instructions
        final SchemaRoot schemaRoot = gson.fromJson(readFile(schemaPath), SchemaRoot.class);

        //create data collector to store xml values
        // NOTE: this could implement a storage interface if became to large for memory
        final Map<String, String> xmlDataStore = new HashMap<>();

        //generate xml processors from the schema file provided that will write to the data store
        final Map<String, XMLProcessor.LocationProcessor> processorMap = XMLProcessorMapFactory.createProcessorMap(schemaRoot, xmlDataStore);

        //create xml processor instance using the newly created processor map
        final XMLProcessor processor = new XMLProcessor(processorMap);

        //process xml which will store collected values in the 'xmlDataStore' map interface
        try(final FileReader fileReader = new FileReader(ResourceUtils.getFile(xmlPath))){
            //create basic Java XMLStreamReader from file reader
            final XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fileReader);
            //run processor using stream reader
            processor.process(xmlReader);
        }

        //output captured data to console
        LOGGER.debug("Captured Data: {}", xmlDataStore);

        // generate and write json using schema and xmlDataStore to StringWriter, this could easily be modified to write to a file stream if became to large
        final StringWriter writer = new StringWriter();
        JsonGenerator.toJson(schemaRoot, xmlDataStore, writer);
        return writer.toString();
    }

    private static String readFile(String path) throws IOException {
        return Files.readString(ResourceUtils.getFile(path).toPath());
    }
}