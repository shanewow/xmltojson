package com.shanewow.xmltojson.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.stream.XMLInputFactory;

@Configuration
public class XMLConfig {

    @Bean
    public XMLInputFactory xmlConfig() {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        // disable external entities
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        return factory;
    }

}
