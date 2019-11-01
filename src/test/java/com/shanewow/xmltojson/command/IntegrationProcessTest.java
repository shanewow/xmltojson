package com.shanewow.xmltojson.command;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IntegrationProcessTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationProcessTest.class);

    @Autowired
    private Gson gson;

    @Autowired
    private XMLInputFactory xmlInputFactory;

    @Test
    void validateSchema() throws IOException {
        final Process process = new Process(gson, xmlInputFactory);
        final String actualOutput = process.validateSchema("classpath:mock-schema.json");
        LOGGER.info(actualOutput);
        assertEquals(
                Files.readString(ResourceUtils.getFile("classpath:expected-schema-output.json").toPath()),
                actualOutput
        );
    }

    @Test
    void processSchema__WithDefaultOutputPath() throws IOException, XMLStreamException {
        final Process process = new Process(gson, xmlInputFactory);
        final String actualOutput = process.processSchema("classpath:mock-schema.json", "classpath:mock-input.xml", "");
        LOGGER.info(actualOutput);
        assertEquals(
                Files.readString(ResourceUtils.getFile("classpath:expected-output.json").toPath()),
                actualOutput
        );
    }

    @Test
    void processSchema__WithOutputPath() throws IOException, XMLStreamException {
        final Process process = new Process(gson, xmlInputFactory);
        final String actualOutput = process.processSchema("classpath:mock-schema.json", "classpath:mock-input.xml", "test-output/test.json");
        LOGGER.info(actualOutput);

        //validate message
        assertEquals(
                "Successfully wrote json to output path: test-output/test.json",
                actualOutput
        );

        //validate file contents
        assertEquals(
                Files.readString(ResourceUtils.getFile("classpath:expected-output.json").toPath()),
                Files.readString(ResourceUtils.getFile("test-output/test.json").toPath())
        );
    }
}