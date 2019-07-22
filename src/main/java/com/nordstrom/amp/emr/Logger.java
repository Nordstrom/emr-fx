package com.nordstrom.amp.emr;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.Hashtable;

public class Logger {
    private org.slf4j.Logger logger;
    private LogItem logItem = new LogItem();
    private ObjectMapper mapper = new ObjectMapper();
    private String source;

    public enum Fields {
        MediaType, Url, Length, Id
    }

    private Logger(org.slf4j.Logger logger, String source) {
        this.logger = logger;
        this.source = source;
    }

    public org.slf4j.Logger getLogger() {
        return logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);

        Logger parent = new Logger(logger, null);

        return parent;
    }

    public static Logger getLogger(Class<?> clazz, String source) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);

        Logger parent = new Logger(logger, source);

        return parent;
    }

    public void error(String msg) {
        error(msg, null);
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void error(String msg, Exception ex) {
        String json = execute(Status.error, msg, null);
        logger.error(json);
    }

    public void warn(String msg, Exception ex) {
        String json = execute(Status.warn, msg, ex);
        logger.warn(json);
    }

    public void info(String msg) {
        String json = execute(Status.info, msg, null);
        logger.info(json);
    }

    public String execute(Status status, String msg, Exception ex) {

        logItem.setMessage(msg);
        logItem.setStatus(status);
        logItem.setSource(source);

        try {
            String json = mapper.writeValueAsString(logItem);

            logItem = new LogItem();
            return json;
        } catch (JsonProcessingException e) {
        }

        logItem = new LogItem();
        return null;
    }

    public Logger withDuration(long milliSeconds) {
        logItem.setDurationInMilliseconds(milliSeconds);
        return this;
    }

    public Logger withParam(Fields key, String value) {
        return withParam(key.toString(), value);
    }

    public Logger withParam(String key, String value) {
        logItem.getParams().put(key, value);
        return this;
    }

    @JsonInclude(Include.NON_NULL)
    public static class LogItem {
        @JsonProperty
        private Status status;

        @JsonProperty
        private String message;

        @JsonProperty
        private String source;

        @JsonProperty
        private long durationInMilliseconds;

        @JsonProperty
        private Dictionary<String, String> params = new Hashtable<>();

        public Dictionary<String, String> getParams() {
            return params;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public long getDurationInMilliseconds() {
            return durationInMilliseconds;
        }

        public void setDurationInMilliseconds(long durationInMilliseconds) {
            this.durationInMilliseconds = durationInMilliseconds;
        }
    }

    public enum Status {
        error, warn, info
    }
}

