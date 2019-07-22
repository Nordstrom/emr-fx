package com.nordstrom.amp.emr;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class Reduce extends Reducer<Text, Text, Text, Text> {

	private final static Logger log = LoggerFactory.getLogger(Reduce.class);
    private ConfigurableListableBeanFactory factory;
    private Module module;


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
	protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		
		try (AutoLogTimer timer = new AutoLogTimer(log, "reduce")) {

            try {
                String env = context.getConfiguration().get("nordstrom.environment");

                initBeans(env);
                
                log.info("reduce: key:[{}]", key.toString());
                module.setReduceContext(context);
                module.reduce(key, values, context);
                
            } catch (Exception ex) {
                log.error(String.format("Reduce: Error processing: %s", concatValues(values)), ex);
            }
        }
    }
	
	private String concatValues(Iterable<Text> values) {
		Iterator<Text> iter = values.iterator();
		StringBuilder strBuilder = new StringBuilder();
		while (iter.hasNext()) {
			strBuilder.append(iter.next().toString()).append(", ");
		}
		return strBuilder.toString();
	}

}
