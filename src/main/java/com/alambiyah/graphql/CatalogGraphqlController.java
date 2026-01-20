package com.alambiyah.graphql;

import com.alambiyah.app.Category;
import com.alambiyah.app.Product;
import com.alambiyah.repository.CategoryRepository;
import com.alambiyah.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
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

    @MutationMapping
    public Category createCategory(@Argument CreateCategoryInput input) {
        Category category = new Category(null, input.name());
        if (input.parentId() != null) {
            category.setParent(resolveCategory(input.parentId()));
        }
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument String id, @Argument UpdateCategoryInput input) {
        Category category = categoryRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (input.name() != null) {
            category.setName(input.name());
        }
        if (input.parentId() != null) {
            category.setParent(resolveOptionalCategory(input.parentId()));
        }
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument String id) {
        UUID categoryId = UUID.fromString(id);
        if (!categoryRepository.existsById(categoryId)) {
            return false;
        }
        categoryRepository.deleteById(categoryId);
        return true;
    }

    @MutationMapping
    public Product createProduct(@Argument CreateProductInput input) {
        Product product = new Product(null, input.name(), input.sku(), input.price());
        if (input.categoryId() != null) {
            product.setCategory(resolveCategory(input.categoryId()));
        }
        return productRepository.save(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument String id, @Argument UpdateProductInput input) {
        Product product = productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (input.name() != null) {
            product.setName(input.name());
        }
        if (input.sku() != null) {
            product.setSku(input.sku());
        }
        if (input.price() != null) {
            product.setPrice(input.price());
        }
        if (input.categoryId() != null) {
            product.setCategory(resolveOptionalCategory(input.categoryId()));
        }
        return productRepository.save(product);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument String id) {
        UUID productId = UUID.fromString(id);
        if (!productRepository.existsById(productId)) {
            return false;
        }
        productRepository.deleteById(productId);
        return true;
    }

    private Category resolveCategory(String id) {
        return categoryRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    private Category resolveOptionalCategory(String id) {
        if (id.isBlank()) {
            return null;
        }
        return resolveCategory(id);
    }

    public record CreateCategoryInput(String name, String parentId) {
    }

    public record UpdateCategoryInput(String name, String parentId) {
    }

    public record CreateProductInput(
            String name,
            String sku,
            BigDecimal price,
            String categoryId
    ) {
    }

    public record UpdateProductInput(
            String name,
            String sku,
            BigDecimal price,
            String categoryId
    ) {
    }
}
