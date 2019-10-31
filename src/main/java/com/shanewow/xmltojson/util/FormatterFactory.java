package com.shanewow.xmltojson.util;

import com.shanewow.xmltojson.model.schema.SchemaItem;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class FormatterFactory {

    private static final Map<String, String> STATE_MAP = Collections.unmodifiableMap(Map.of(

            //abbreviation to full
            "MI", "Michigan",
            "OH", "Ohio",
            //TODO complete state mapping

            //full to abbreviation
            "Michigan", "MI",
            "Ohio", "OH"
            //TODO complete state mapping
    ));

    private static final Map<String, String> GENDER_MAP = Collections.unmodifiableMap(Map.of(

            //abbreviation to full
            "f", "female",
            "m", "male",

            //full to abbreviation
            "Female", "f",
            "Male", "m"
    ));

    private static Map<SchemaItem.FORMATTER, Function<String, String>> FORMATTERS = Collections.unmodifiableMap(Map.of(
            //create date of birth formatter
            SchemaItem.FORMATTER.dateOfBirthToAge, input -> {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate birthday = LocalDate.parse(input, formatter);
                LocalDate today = LocalDate.now();

                Period p = Period.between(birthday, today);
                return Integer.toString(p.getYears());
            },

            //create state formatter
            SchemaItem.FORMATTER.state, input -> {
                return STATE_MAP.get(input);
            },

            //create state formatter
            SchemaItem.FORMATTER.gender, input -> {
                return GENDER_MAP.get(input);
            }


    ));

    public static Function<String, String> getFormatter(SchemaItem.FORMATTER formatter) {
        return FORMATTERS.get(formatter);
    }

}
