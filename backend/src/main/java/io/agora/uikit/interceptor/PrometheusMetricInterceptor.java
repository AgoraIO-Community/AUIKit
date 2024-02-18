package io.agora.uikit.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.agora.uikit.metric.PrometheusMetric;
import io.prometheus.client.Histogram;

@Component
public class PrometheusMetricInterceptor implements HandlerInterceptor {
    @Autowired
    private PrometheusMetric prometheusMetric;

    private Histogram.Timer histogramRequestTimer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        histogramRequestTimer = prometheusMetric.getHttpRequestsDurationSecondsHistogram()
                .labels(request.getRequestURI(), String.valueOf(response.getStatus())).startTimer();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception exception) throws Exception {
        histogramRequestTimer.observeDuration();
    }
}
