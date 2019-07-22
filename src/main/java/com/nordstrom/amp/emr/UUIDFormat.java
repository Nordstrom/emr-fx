package com.nordstrom.amp.emr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DynamoDBTypeConverted(converter=UUIDFormat.Converter.class)
public @interface UUIDFormat {

    public static class Converter implements DynamoDBTypeConverter<String, UUID> {

        @Override
        public String convert(UUID uuid) {
            return uuid.toString();
        }

        @Override
        public UUID unconvert(String object) {
            return UUID.fromString(object);
        }

    }
}
