
package com.shanewow.xmltojson.model.schema;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SchemaItem {

    public enum TYPE {integer, number, string, object, array}

    public enum FORMATTER {dateOfBirthToAge, state, gender}

    @SerializedName("$id")
    private String id;
    private TYPE type;
    private String title;
    private SchemaItem items;
    private List<String> required = null;
    private Map<String, SchemaItem> properties = null;

    private String xmlSourcePath;
    private FORMATTER xmlFormatter;
}
