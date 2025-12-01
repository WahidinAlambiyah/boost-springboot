package com.example.boost.category.repository;

import com.example.boost.category.graph.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface CategoryNodeRepository extends Neo4jRepository<CategoryNode, Long> {
    CategoryNode findByName(String name);
}
