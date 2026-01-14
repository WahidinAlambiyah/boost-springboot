package com.example.graphqlusers.graphql;

import com.example.graphqlusers.auth.AuthContext;
import org.dataloader.DataLoaderRegistry;

public record GraphQLContext(
        AuthContext authContext,
        DataLoaderRegistry dataLoaderRegistry
) {
}
