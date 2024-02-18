package io.agora.uikit.config;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import io.agora.uikit.service.IEMAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ChatRoomAPIClient {

    @Value("${chatRoom.domain}")
    private String domain;

    @Bean
    public IEMAPIService emAPIService() {
        return Feign.builder()
                .logger(new Slf4jLogger())
                .logLevel(feign.Logger.Level.FULL)
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IEMAPIService.class, domain);
    }
}
