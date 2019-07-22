package com.nordstrom.amp.emr;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Module {
	protected static final Logger log = LoggerFactory.getLogger(Module.class);

    @Autowired
    protected ObjectMapper objectMapper;
    private Text keyText = new Text();
    private Text valueText = new Text();

    public abstract void process(Text row) throws Exception;
    public void reduce(Text key, java.lang.Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context) throws Exception{

        reduceWriteContext("1", "1");

        context.setStatus("COMPLETED");
    }

    private Mapper<LongWritable, Text, Text, Text>.Context mapContext;
    private Reducer<Text, Text, Text, Text>.Context reduceContext;

    public void setMapContext(Mapper<LongWritable, Text, Text, Text>.Context context){
        mapContext = context;
    }
    public void setReduceContext(Reducer<Text, Text, Text, Text>.Context context){
        reduceContext = context;
    }
    public void mapWriteContext(String text, String value){
        keyText.set(text);
        valueText.set(value);
        try {
            mapContext.write(keyText, valueText);
        } catch (Exception ex) {
            log.error("Error writing to context", ex);
        }
    }

    public void reduceWriteContext(String text, String value){
        keyText.set(text);
        valueText.set(value);
        try {
            reduceContext.write(keyText, valueText);
        } catch (Exception ex) {
            log.error("Error writing to context", ex);
        }
    }
}
