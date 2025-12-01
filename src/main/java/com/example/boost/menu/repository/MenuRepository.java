package com.example.boost.menu.repository;

import com.example.boost.menu.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, String> {
}
