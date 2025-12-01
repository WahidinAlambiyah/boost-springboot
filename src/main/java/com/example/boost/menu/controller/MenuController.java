package com.example.boost.menu.controller;

import com.example.boost.menu.model.Menu;
import com.example.boost.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) {
        this.service = service;
    }

    @GetMapping
    public List<Menu> list() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<Menu> create(@Valid @RequestBody Menu menu) {
        return ResponseEntity.ok(service.create(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> update(@PathVariable String id, @Valid @RequestBody Menu menu) {
        return ResponseEntity.ok(service.update(id, menu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
