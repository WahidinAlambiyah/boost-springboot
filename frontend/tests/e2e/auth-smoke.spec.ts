import { expect, test } from "@playwright/test";

test("smoke: login -> authorized page -> forbidden page", async ({ page }) => {
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
          permissions: ["CLASS_READ"],
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

  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();

  await page.evaluate(() => {
    localStorage.setItem("refreshToken", "seed-refresh-token");
  });

  await page.getByRole("link", { name: "Coba ke Dashboard" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole("heading", { name: "Dashboard" })).toBeVisible();

  await page.goto("/catalog");
  await expect(page.getByRole("heading", { name: "Catalog Module" })).toBeVisible();

  await page.goto("/admin");
  await expect(page).toHaveURL(/\/forbidden$/);
  await expect(page.getByRole("heading", { name: "Forbidden" })).toBeVisible();
  await expect(page.getByText("403")).toBeVisible();
});
