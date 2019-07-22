package com.nordstrom.amp.emr;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class Map extends Mapper<LongWritable, Text, Text, Text> {

	private final static Logger log = LoggerFactory.getLogger(Map.class);
    private static ConfigurableListableBeanFactory factory;

    private static Module module;

    private void initBeans(String env) {
        log.info("initBeans");

        if (factory == null) {
            factory = Program.getFactory(env);
        }

        if (module == null) {
            module = factory.getBean(Program.getModule());
        }
    }

	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		 try (AutoLogTimer timer = new AutoLogTimer(log, "map")) {

	            try {
	                String env = context.getConfiguration().get("nordstrom.environment");

	                initBeans(env);

	                log.info("map: key=[{}] ============================================================================", key.toString());
	                module.setMapContext(context);
	                module.process(value);
	            } catch (Exception ex) {
	                log.error(String.format("Map: Error processing: %s", value), ex);
	            }
	        }
	}	

}
