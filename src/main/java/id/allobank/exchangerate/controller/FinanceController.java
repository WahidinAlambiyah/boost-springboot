package id.allobank.exchangerate.controller;

import id.allobank.exchangerate.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/data")
@RequiredArgsConstructor
public class FinanceController {

    private static final Logger log = LoggerFactory.getLogger(FinanceController.class);
    private final FinanceService service;

    @GetMapping("/{type}")
    public ResponseEntity<List<Map<String, Object>>> get(@PathVariable String type) {
        log.info("Request: {}", type);
        return ResponseEntity.ok(service.getData(type));
    }
}
