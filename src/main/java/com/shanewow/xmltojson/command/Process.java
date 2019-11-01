package com.shanewow.xmltojson.command;

import com.google.gson.Gson;
import com.shanewow.xmltojson.config.Defaults;
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
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@ShellComponent
public class Process {

    private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);

    private final Gson gson;
    private XMLInputFactory xmlInputFactory;

    public Process(Gson gson, XMLInputFactory xmlInputFactory) {
        this.gson = gson;
        this.xmlInputFactory = xmlInputFactory;
    }

    @ShellMethod("Load and output the parsed schema to the console")
    public String validateSchema(@ShellOption String path) throws IOException {
        final SchemaRoot schemaRoot = gson.fromJson(readFile(path), SchemaRoot.class);
        return gson.toJson(schemaRoot);
    }

    @ShellMethod("Process xml file using the json schema")
    public String processSchema(
            @ShellOption String schemaPath,
            @ShellOption String xmlPath,
            @ShellOption(defaultValue = Defaults.BLANK_XML_PATH) String outputPath
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
        try (final FileReader fileReader = new FileReader(ResourceUtils.getFile(xmlPath))) {
            //create basic Java XMLStreamReader from file reader
            final XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(fileReader);
            //run processor using stream reader
            processor.process(xmlReader);
        }

        //debug the data captured from the xml stream
        LOGGER.info("Captured Data: {}", xmlDataStore);

        //create file writer if outputPath is set otherwise use string writer
        final Writer writer = Defaults.BLANK_XML_PATH.equals(outputPath)
                ? new StringWriter()
                : new FileWriter(createDirectoriesAndFile(outputPath));

        //generate and write json to writer
        JsonGenerator.toJson(schemaRoot, xmlDataStore, writer);

        //if string writer, output string to console
        if (writer instanceof StringWriter) {
            return writer.toString();
        }

        //output message with output path to console
        return String.format("Successfully wrote json to output path: %s", outputPath);

    }

    private static String readFile(String path) throws IOException {
        return Files.readString(ResourceUtils.getFile(path).toPath());
    }

    private static File createDirectoriesAndFile(String filePath) throws IOException {
        File file = new File(filePath);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        boolean fileWasCreated = file.createNewFile();
        if (fileWasCreated) {
            LOGGER.debug("Created new file: {}", filePath);
        } else {
            LOGGER.debug("File already exists here: {}", filePath);
        }
        return file;
    }
}