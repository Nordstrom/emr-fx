package com.nordstrom.amp.emr;


import java.io.IOException;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Special class to handle serialization to and from ISO8651
 */
public class JacksonJodaTimeSerializer extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public JacksonJodaTimeSerializer() {
        super("JodaTimeSerializer");
        super.addSerializer(DateTime.class, new JsonSerializer<DateTime>() {
            @Override
            public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeString(JodaDateTimeFormatter.ISO8651.print(value));
            }
        });

        super.addDeserializer(DateTime.class, new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonParser parser, DeserializationContext context)
                    throws IOException {
                return JodaDateTimeFormatter.ISO8651.parseDateTime(parser.getText());
            }
        });
    }
}
