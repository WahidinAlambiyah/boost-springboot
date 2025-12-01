package com.example.boost.menu.service;

import com.example.boost.event.service.DomainEventService;
import com.example.boost.menu.model.Menu;
import com.example.boost.menu.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class MenuService {

    private final MenuRepository repository;
    private final Supplier<String> ulidSupplier;
    private final DomainEventService eventService;

    public MenuService(MenuRepository repository, Supplier<String> ulidSupplier, DomainEventService eventService) {
        this.repository = repository;
        this.ulidSupplier = ulidSupplier;
        this.eventService = eventService;
    }

    public List<Menu> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Menu create(Menu menu) {
        menu.setId(ulidSupplier.get());
        Menu saved = repository.save(menu);
        eventService.record("Menu", saved.getId(), "MENU_CREATED", saved);
        return saved;
    }

    @Transactional
    public Menu update(String id, Menu menu) {
        menu.setId(id);
        Menu saved = repository.save(menu);
        eventService.record("Menu", saved.getId(), "MENU_UPDATED", saved);
        return saved;
    }

    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
        eventService.record("Menu", id, "MENU_DELETED", null);
    }
}
