package com.alambiyah.graphql;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class CatalogGraphqlControllerTest {
    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void listProductsSupportsSortingAndFiltering() {
        String categoryId = createCategory("Category-" + UUID.randomUUID());
        createProduct("Product A", "SKU-A-" + UUID.randomUUID(), new BigDecimal("19.99"), categoryId);
        createProduct("Product B", "SKU-B-" + UUID.randomUUID(), new BigDecimal("29.99"), categoryId);

        graphQlTester.document("""
                        query($filter: ProductFilter, $sortBy: String, $sortDirection: SortDirection) {
                          products(filter: $filter, sortBy: $sortBy, sortDirection: $sortDirection) {
                            sku
                          }
                        }
                        """)
                .variable("filter", Map.of("sku", "SKU-A"))
                .variable("sortBy", "sku")
                .variable("sortDirection", "ASC")
                .execute()
                .path("products")
                .entityList(Object.class)
                .hasSize(1);
    }

    @Test
    void listCategoriesSupportsFilteringByParent() {
        String parentId = createCategory("Parent-" + UUID.randomUUID());
        createCategoryWithParent("Child-" + UUID.randomUUID(), parentId);

        String name = graphQlTester.document("""
                        query($filter: CategoryFilter) {
                          categories(filter: $filter) {
                            name
                          }
                        }
                        """)
                .variable("filter", Map.of("parentId", parentId))
                .execute()
                .path("categories[0].name")
                .entity(String.class)
                .get();

        Assertions.assertTrue(name.startsWith("Child-"));
    }

    private String createCategory(String name) {
        return createCategoryWithParent(name, null);
    }

    private String createCategoryWithParent(String name, String parentId) {
        Map<String, Object> input = parentId == null
                ? Map.of("name", name)
                : Map.of("name", name, "parentId", parentId);
        return graphQlTester.document("""
                        mutation($input: CreateCategoryInput!) {
                          createCategory(input: $input) {
                            id
                          }
                        }
                        """)
                .variable("input", input)
                .execute()
                .path("createCategory.id")
                .entity(String.class)
                .get();
    }

    private String createProduct(String name, String sku, BigDecimal price, String categoryId) {
        Map<String, Object> input = Map.of(
                "name", name,
                "sku", sku,
                "price", price,
                "categoryId", categoryId
        );
        return graphQlTester.document("""
                        mutation($input: CreateProductInput!) {
                          createProduct(input: $input) {
                            id
                          }
                        }
                        """)
                .variable("input", input)
                .execute()
                .path("createProduct.id")
                .entity(String.class)
                .get();
    }
}
