package io.agora.uikit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.agora.uikit.interceptor.PrometheusMetricInterceptor;
import io.agora.uikit.interceptor.TraceIdInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private TraceIdInterceptor traceIdInterceptor;
    @Autowired
    private PrometheusMetricInterceptor prometheusMetricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(prometheusMetricsInterceptor).addPathPatterns("/**");
        registry.addInterceptor(traceIdInterceptor).addPathPatterns("/**");
    }
}
