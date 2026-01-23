package com.alambiyah.graphql;

import com.alambiyah.app.Category;
import com.alambiyah.app.Product;
import com.alambiyah.repository.CategoryRepository;
import com.alambiyah.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public List<Category> categories(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument SortDirection sortDirection,
            @Argument CategoryFilter filter
    ) {
        return categoryRepository.findAll(
                buildCategorySpecification(filter),
                resolvePageable(page, size, sortBy, sortDirection, "name", Set.of("name"))
        ).getContent();
    }

    @QueryMapping
    public Category category(@Argument String id) {
        return categoryRepository.findById(UUID.fromString(id)).orElse(null);
    }

    @QueryMapping
    public List<Product> products(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument SortDirection sortDirection,
            @Argument ProductFilter filter
    ) {
        return productRepository.findAll(
                buildProductSpecification(filter),
                resolvePageable(page, size, sortBy, sortDirection, "name", Set.of("name", "sku", "price"))
        ).getContent();
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

    private Pageable resolvePageable(
            Integer page,
            Integer size,
            String sortBy,
            SortDirection sortDirection,
            String defaultSort,
            Set<String> allowedSorts
    ) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null ? 20 : Math.max(size, 1);
        String sortField = resolveSortField(sortBy, defaultSort, allowedSorts);
        Sort.Direction direction = resolveSortDirection(sortDirection);
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }

    private Sort.Direction resolveSortDirection(SortDirection sortDirection) {
        if (sortDirection == null) {
            return Sort.Direction.ASC;
        }
        return sortDirection == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveSortField(String sortBy, String defaultSort, Set<String> allowedSorts) {
        String field = (sortBy == null || sortBy.isBlank()) ? defaultSort : sortBy;
        if (!allowedSorts.contains(field)) {
            throw new IllegalArgumentException("Unsupported sort field: " + field);
        }
        return field;
    }

    private Specification<Category> buildCategorySpecification(CategoryFilter filter) {
        if (filter == null) {
            return emptySpecification();
        }
        Specification<Category> specification = this.<Category>containsIgnoreCase("name", filter.name());
        if (filter.parentId() != null && !filter.parentId().isBlank()) {
            UUID parentId = UUID.fromString(filter.parentId());
            specification = specification.and((root, query, builder) -> builder.equal(root.get("parent").get("id"), parentId));
        }
        return specification;
    }

    private Specification<Product> buildProductSpecification(ProductFilter filter) {
        if (filter == null) {
            return emptySpecification();
        }
        Specification<Product> specification = this.<Product>containsIgnoreCase("name", filter.name())
                .and(this.<Product>containsIgnoreCase("sku", filter.sku()));
        if (filter.categoryId() != null && !filter.categoryId().isBlank()) {
            UUID categoryId = UUID.fromString(filter.categoryId());
            specification = specification.and((root, query, builder) -> builder.equal(root.get("category").get("id"), categoryId));
        }
        return specification;
    }

    private <T> Specification<T> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return emptySpecification();
        }
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get(field)), pattern);
    }

    private <T> Specification<T> emptySpecification() {
        return (root, query, builder) -> null;
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

    public record CategoryFilter(String name, String parentId) {
    }

    public record ProductFilter(String name, String sku, String categoryId) {
    }
}
