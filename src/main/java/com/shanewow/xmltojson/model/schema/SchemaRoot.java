
package com.shanewow.xmltojson.model.schema;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchemaRoot extends SchemaItem {
    @SerializedName("$schema")
    private String schema;
}
