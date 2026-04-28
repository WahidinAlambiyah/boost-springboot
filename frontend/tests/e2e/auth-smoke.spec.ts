import { expect, test } from "@playwright/test";

test("smoke: login -> open modules -> logout", async ({ page }) => {
  await page.route("**/api/auth/login", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: {
          accessToken: "access-token",
          refreshToken: "refresh-token",
          tokenType: "Bearer",
        },
      }),
    });
  });

  await page.route("**/api/auth/refresh", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: {
          accessToken: "access-token",
          refreshToken: "refresh-token",
          tokenType: "Bearer",
        },
      }),
    });
  });

  await page.route("**/api/users/me", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: {
          id: "1",
          username: "demo",
          email: "demo@example.com",
          isActive: true,
          roles: ["USER"],
          permissions: ["CLASS_READ", "ENROLLMENT_READ", "ATTENDANCE_READ"],
          createdAt: "2026-01-01T00:00:00.000Z",
        },
      }),
    });
  });

  await page.route("**/api/catalog", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: [],
      }),
    });
  });

  await page.route("**/api/enrollment", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: [],
      }),
    });
  });

  await page.route("**/api/attendance", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: [],
      }),
    });
  });

  await page.route("**/api/auth/logout", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "logout",
        data: null,
      }),
    });
  });

  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();

  await page.getByLabel("Username").fill("demo");
  await page.getByLabel("Password").fill("secret");
  await page.getByRole("button", { name: "Login" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole("heading", { name: "Dashboard" })).toBeVisible();

  await page.goto("/catalog");
  await expect(page.getByRole("heading", { name: "Catalog Module" })).toBeVisible();

  await page.goto("/enrollment");
  await expect(page.getByRole("heading", { name: "Enrollment Module" })).toBeVisible();

  await page.goto("/attendance");
  await expect(page.getByRole("heading", { name: "Attendance Module" })).toBeVisible();

  await page.getByRole("button", { name: "Logout" }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
});
