package com.alambiyah.graphql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class UserRoleGraphqlControllerTest {
    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void userCrudFlow() {
        String roleName = "MANAGER-" + UUID.randomUUID();
        String roleId = createRole(roleName);

        Map<String, Object> createInput = new HashMap<>();
        createInput.put("username", "dave-" + UUID.randomUUID());
        createInput.put("email", "dave-" + UUID.randomUUID() + "@example.com");
        createInput.put("fullName", "Dave Test");
        createInput.put("roleIds", List.of(roleId));

        String userId = graphQlTester.document("""
                        mutation($input: CreateUserInput!) {
                          createUser(input: $input) {
                            id
                            username
                            roles { id name }
                          }
                        }
                        """)
                .variable("input", createInput)
                .execute()
                .path("createUser.id")
                .entity(String.class)
                .get();

        graphQlTester.document("""
                        query($id: ID!) {
                          user(id: $id) {
                            id
                            username
                            roles { id name }
                          }
                        }
                        """)
                .variable("id", userId)
                .execute()
                .path("user.roles")
                .entityList(Object.class)
                .hasSize(1);

        Map<String, Object> updateInput = Map.of("email", "updated-" + UUID.randomUUID() + "@example.com");

        graphQlTester.document("""
                        mutation($id: ID!, $input: UpdateUserInput!) {
                          updateUser(id: $id, input: $input) {
                            id
                            email
                          }
                        }
                        """)
                .variable("id", userId)
                .variable("input", updateInput)
                .execute()
                .path("updateUser.email")
                .hasValue();

        Boolean deleted = graphQlTester.document("""
                        mutation($id: ID!) {
                          deleteUser(id: $id)
                        }
                        """)
                .variable("id", userId)
                .execute()
                .path("deleteUser")
                .entity(Boolean.class)
                .get();

        Assertions.assertTrue(deleted);
    }

    @Test
    void roleCrudFlow() {
        String roleName = "SUPPORT-" + UUID.randomUUID();
        String roleId = createRole(roleName);

        Map<String, Object> updateInput = Map.of("name", roleName + "-UPDATED");

        String updatedName = graphQlTester.document("""
                        mutation($id: ID!, $input: UpdateRoleInput!) {
                          updateRole(id: $id, input: $input) {
                            id
                            name
                          }
                        }
                        """)
                .variable("id", roleId)
                .variable("input", updateInput)
                .execute()
                .path("updateRole.name")
                .entity(String.class)
                .get();

        Assertions.assertTrue(updatedName.endsWith("UPDATED"));

        Boolean deleted = graphQlTester.document("""
                        mutation($id: ID!) {
                          deleteRole(id: $id)
                        }
                        """)
                .variable("id", roleId)
                .execute()
                .path("deleteRole")
                .entity(Boolean.class)
                .get();

        Assertions.assertTrue(deleted);
    }

    @Test
    void listQueriesSupportSortingAndFiltering() {
        graphQlTester.document("""
                        query($filter: UserFilter, $sortBy: String, $sortDirection: SortDirection) {
                          users(filter: $filter, sortBy: $sortBy, sortDirection: $sortDirection) {
                            username
                          }
                        }
                        """)
                .variable("filter", Map.of("username", "alice"))
                .variable("sortBy", "username")
                .variable("sortDirection", "ASC")
                .execute()
                .path("users")
                .entityList(Object.class)
                .hasSize(1);

        graphQlTester.document("""
                        query($sortBy: String, $sortDirection: SortDirection) {
                          roles(sortBy: $sortBy, sortDirection: $sortDirection) {
                            name
                          }
                        }
                        """)
                .variable("sortBy", "name")
                .variable("sortDirection", "DESC")
                .execute()
                .path("roles[0].name")
                .entity(String.class)
                .isEqualTo("USER");
    }

    private String createRole(String name) {
        Map<String, Object> input = Map.of("name", name);
        return graphQlTester.document("""
                        mutation($input: CreateRoleInput!) {
                          createRole(input: $input) {
                            id
                            name
                          }
                        }
                        """)
                .variable("input", input)
                .execute()
                .path("createRole.id")
                .entity(String.class)
                .get();
    }
}
