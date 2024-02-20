package io.agora.uikit.config;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import io.agora.uikit.service.IRtcChannelAPIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RtcChannelAPIClient {

    @Value("${kickOut.domain}")
    private String url;

    @Bean
    public IRtcChannelAPIService rtcChannelAPIService() {
        return Feign.builder()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IRtcChannelAPIService.class, url);
    }
}
