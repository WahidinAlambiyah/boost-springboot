package com.alambiyah.graphql;

import com.alambiyah.app.Category;
import com.alambiyah.app.Product;
import com.alambiyah.repository.CategoryRepository;
import com.alambiyah.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Transactional
public class CatalogGraphqlController {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogGraphqlController(
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    @QueryMapping
    public Category category(@Argument String id) {
        return categoryRepository.findById(UUID.fromString(id)).orElse(null);
    }

    @QueryMapping
    public List<Product> products() {
        return productRepository.findAll();
    }

    @QueryMapping
    public Product product(@Argument String id) {
        return productRepository.findById(UUID.fromString(id)).orElse(null);
    }
}
