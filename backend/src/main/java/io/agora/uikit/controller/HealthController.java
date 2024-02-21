package io.agora.uikit.controller;

import io.agora.uikit.bean.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {

    /**
     * Check
     *
     * @return
     */
    @RequestMapping("/check")
    public R<Void> check() {
        return R.success(null);
    }
}