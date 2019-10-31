package com.shanewow.xmltojson.config;

import org.springframework.context.annotation.Bean;

import javax.xml.stream.XMLInputFactory;

public class XMLConfig {

    @Bean
    public XMLInputFactory xmlConfig() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        // disable external entities
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        return factory;
    }

}
