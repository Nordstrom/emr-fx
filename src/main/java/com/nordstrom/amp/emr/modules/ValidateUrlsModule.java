package com.nordstrom.amp.emr.modules;

import com.nordstrom.amp.emr.Module;
import org.apache.hadoop.io.Text;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.nordstrom.amp.emr.Utility.httpGet;

@Component
public class ValidateUrlsModule extends Module {
    private final static Logger log = LoggerFactory.getLogger(ValidateUrlsModule.class);

    @Override
    public void process(Text row) {
        String url = row.toString();

        try {
            log.info("Url: {}", url);

            mapWriteContext(url, "1");

            List<Header> headers = new ArrayList<>();

            HttpResponse response = httpGet(headers, url, null);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                log.error("Failed to retrieve url:{} statusCode:{}", url, statusCode);
            }
        } catch (Exception ex) {
            log.error("Unable to process url", ex);
        }
    }
}

