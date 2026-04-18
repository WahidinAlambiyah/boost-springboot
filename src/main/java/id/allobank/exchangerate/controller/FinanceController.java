package id.allobank.exchangerate.controller;

import id.allobank.exchangerate.service.FinanceService;
import id.allobank.exchangerate.store.InMemoryDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/data")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService service;

    @GetMapping("/{type}")
    public ResponseEntity<?> get(@PathVariable String type) {
        return ResponseEntity.ok(service.getData(type));
    }
}
