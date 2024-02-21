package io.agora.uikit.metric;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
public class PrometheusMetric {
    @Autowired
    private CollectorRegistry collectorRegistry;

    private Counter httpRequestsCounter;
    private Histogram httpRequestsDurationSecondsHistogram;

    @PostConstruct
    private void init() {
        httpRequestsCounter = Counter.build()
                .name("http_requests_total")
                .help("http requests total")
                .labelNames("api", "code", "message")
                .register(collectorRegistry);

        httpRequestsDurationSecondsHistogram = Histogram.build()
                .name("http_requests_duration_seconds")
                .help("http requests duration seconds")
                .labelNames("api", "status")
                .register(collectorRegistry);

    }
}