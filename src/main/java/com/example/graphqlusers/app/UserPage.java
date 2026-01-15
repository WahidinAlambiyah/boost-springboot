package com.example.graphqlusers.app;

import java.util.List;

public record UserPage(
        List<User> items,
        int page,
        int size,
        long total
) {
}
