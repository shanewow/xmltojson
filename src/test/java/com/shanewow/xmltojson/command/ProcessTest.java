package com.shanewow.xmltojson.command;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ProcessTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTest.class);

    @Test
    void validateSchema() throws IOException {
        final Process process = new Process(new GsonBuilder().setPrettyPrinting().create());
        final String actualOutput = process.validateSchema("classpath:mock-schema.json");
        LOGGER.info(actualOutput);
        assertEquals(
                Files.readString(ResourceUtils.getFile("classpath:expected-schema-output.json").toPath()),
                actualOutput
        );
    }

    @Test
    void processSchema() throws IOException, XMLStreamException {
        final Process process = new Process(new GsonBuilder().setPrettyPrinting().create());
        final String actualOutput = process.processSchema("classpath:mock-schema.json", "classpath:mock-input.xml");
        LOGGER.info(actualOutput);
        assertEquals(
                Files.readString(ResourceUtils.getFile("classpath:expected-output.json").toPath()),
                actualOutput
        );
    }
}