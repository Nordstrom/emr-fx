package com.nordstrom.amp.emr;

import java.util.Random;

import com.nordstrom.amp.emr.modules.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

@org.springframework.context.annotation.Configuration
@Import({ EmrBaseConfig.class })
public class Program {

	private final static Logger log = LoggerFactory.getLogger(Program.class);

	@SuppressWarnings("unchecked")
	static <T extends Module> Class<T> getModule() {
		return (Class<T>) (ValidateUrlsModule.class);
	}
	
	static ConfigurableListableBeanFactory getFactory(String env) {
        log.info("Initializing environment [{}]", env);

        ConfigurableListableBeanFactory factory;

        AnnotationConfigApplicationContext temp = new AnnotationConfigApplicationContext();
        if (env == null) {
            env = "dev";
        }
        
        temp.getEnvironment().setActiveProfiles(env);
        temp.register(Program.class);
        temp.refresh();
        factory = temp.getBeanFactory();
        log.info("Bean factory initialized:{}", env);

        return factory;
    }
	
	public static void main(String[] args) throws Exception {
        try (AutoLogTimer timer = new AutoLogTimer(log, "EMR-JOB")) {
            MDC.put("global", "N/A");

            log.info("Build 2019-07-24 09:30");

            String env = args[1];
            String inputPath = args[2];
            String outputPath = args[3];

            String javaLibraryPath = System.getProperty("java.library.path");

            log.info("env = [{}]", env);
            log.info("input path = [{}]", inputPath);
            log.info("output path = [{}]", outputPath);
            log.info("java.library.path = [{}]", javaLibraryPath);
            log.info("module = [{}]", getModule().getSimpleName());

            Configuration conf = new Configuration();

            // https://hadoop.apache.org/docs/r1.0.4/mapred-default.html
            // This lets you debug locally!!!!!!
            // conf.set("mapred.job.tracker", "local");
            conf.set("nordstrom.environment", env);

            Integer timeout = 30 * 60 * 1000; // milliseconds
            conf.setInt("mapreduce.task.timeout", timeout);
            conf.setInt("mapreduce.map.maxattempts", 1);
            conf.setInt("mapreduce.reduce.maxattempts", 1);
            conf.setDouble("mapreduce.job.reduce.slowstart.completedmaps", 1.0);

            Job job = Job.getInstance(conf, "amp-emr");

            job.setJarByClass(Program.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.setMapperClass(Map.class);
            job.setReducerClass(Reduce.class);
            //job.setInputFormatClass(TextInputFormat.class);
            //job.setOutputFormatClass(TextOutputFormat.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            FileOutputFormat.setOutputPath(job, new Path("emr-output-" + Integer.toString(new Random().nextInt(100000))));

            job.waitForCompletion(true);

            log.info("DONE!!!");
        }
    }

}
