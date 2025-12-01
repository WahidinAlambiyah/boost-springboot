package com.example.boost.category.graph;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Category")
public class CategoryNode {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @Relationship(type = "HAS_CHILD")
    private Set<CategoryNode> children = new HashSet<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<CategoryNode> getChildren() { return children; }
}
