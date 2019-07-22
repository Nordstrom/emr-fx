package com.nordstrom.amp.emr;

import java.io.File;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@PropertySource("classpath:/emr.common.properties")
//@Import(CacheFactory.class)
@ComponentScan({"com.nordstrom.amp.emr"})
public class EmrBaseConfig {

    private final static Logger logger = LoggerFactory.getLogger(EmrBaseConfig.class);

    @Value("${amp.profile.name}")
    private String env;
    @Value("${http.proxyHost:#{null}}")
    private String proxyHost;
    @Value("${http.proxyPort:#{null}}")
    private Integer proxyPort;
    @Value("${aws.profileCredentialsProvider:#{null}}")
    private String profileCredentialsProvider;
    @Value("${aws.clientConfiguration.connectionTimeout:1000}")
    private Integer clientConnectionTimeout;
    @Value("${aws.clientConfiguration.executionTimeout:6000}")
    private Integer clientExecutionTimeout;

    private Region region = null;
    private static ClientConfiguration clientConfiguration;

    @Configuration
    @PropertySource("classpath:/emr.local.properties")
    @Profile("local")
    public static class MigrationLocalConfig {
    }

    @Configuration
    @PropertySource("classpath:/emr.dev.properties")
    @Profile("dev")
    public static class MigrationDevConfig {
    }

    @Configuration
    @PropertySource("classpath:/emr.integ.properties")
    @Profile("integ")
    public static class MigrationIntegConfig {
    }

    @Configuration
    @PropertySource("classpath:/emr.prod.properties")
    @Profile("prod")
    public static class MigrationProdConfig {
    }

    @Bean(name = "localConfig")
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setLocalOverride(true);
        ppc.setOrder(0);

        String location = System.getProperty("user.home") + File.separator + "amp.properties";
        File file = new File(location);
        if (file.exists()) {
            ppc.setLocation(new FileSystemResource(file));
            logger.info("Found properties override in home directory");
        }
        return ppc;
    }


    @Bean
    @DependsOn("credentialProvider")
    @Scope("prototype")
    public AmazonDynamoDBClient getAmazonDynamoDBClient() {

        logger.debug("Creating new instance of AmazonDynamoDBClient initialized to region, " + getCurrentRegion());

        AmazonDynamoDBClient client = new AmazonDynamoDBClient(getCredentialsProvider(), getClientConfiguration());
        client.setRegion(getCurrentRegion());

        return client;
    }

    @Bean
    @Scope("prototype")
    public AmazonS3 getAmazonS3Client() {
        logger.debug("Creating new instance of AmazonS3Client initialized to region, " + region);

        AmazonS3 client = new AmazonS3Client(getCredentialsProvider(), getClientConfiguration());
        client.setRegion(getCurrentRegion());

        return client;
    }

    @Bean
    @Autowired
    public DynamoDBMapper getDynamoDBMapper(AmazonDynamoDBClient client) {
        return new DynamoDBMapper(client);
    }

    @Bean(name = {"credentialProvider"})
    @DependsOn("localConfig")
    @Scope("prototype")
    public AWSCredentialsProvider getCredentialsProvider() {
        if (this.profileCredentialsProvider != null) {
            ProfileCredentialsProvider profileCredentialProvider = new ProfileCredentialsProvider(
                    this.profileCredentialsProvider);
            return profileCredentialProvider;
        } else {
            return new DefaultAWSCredentialsProviderChain();
        }
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE);
        mapper.registerModule(new JacksonJodaTimeSerializer());
        return mapper;
    }

    private ClientConfiguration getClientConfiguration() {
        if (clientConfiguration == null) {
            clientConfiguration = new ClientConfiguration();
            clientConfiguration.setConnectionTimeout(clientConnectionTimeout);
            clientConfiguration.setClientExecutionTimeout(clientExecutionTimeout);
            clientConfiguration.setUseGzip(false);

            if (!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort)) {
                clientConfiguration.setProxyHost(proxyHost);
                clientConfiguration.setProxyPort(proxyPort);
            }
        }
        return clientConfiguration;
    }

    private Region getCurrentRegion() {
        if (region != null)
            return region;

        logger.debug("Defaulting to us-west-2");
        region = Region.getRegion(Regions.US_WEST_2);

        return region;
    }
}
