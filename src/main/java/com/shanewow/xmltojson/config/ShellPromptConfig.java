package com.shanewow.xmltojson.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellPromptConfig {
    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("xmltojson:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
