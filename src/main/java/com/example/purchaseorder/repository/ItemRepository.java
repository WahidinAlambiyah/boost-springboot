package com.example.purchaseorder.repository;

import com.example.purchaseorder.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByIdAndDeletedFalse(Long id);

    Page<Item> findAllByDeletedFalse(Pageable pageable);
}
