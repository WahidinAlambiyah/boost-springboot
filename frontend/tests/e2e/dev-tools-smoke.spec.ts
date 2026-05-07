import { expect, test } from "@playwright/test";

const authPayload = {
  accessToken: "dev-tools-access-token",
  refreshToken: "dev-tools-refresh-token",
  tokenType: "Bearer",
};

const userProfilePayload = {
  id: "dev-user-1",
  username: "devtools",
  email: "devtools@example.com",
  isActive: true,
  roles: ["USER"],
  // TODO: switch this fallback to DEV_TOOLS_READ when the backend manages dev tools permissions.
  permissions: ["USER_READ"],
  createdAt: "2026-01-01T00:00:00.000Z",
};

test("smoke: fallback dev tools permission opens CRUD demo", async ({ page }) => {
  await page.route("**/api/auth/login", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: authPayload,
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
        data: authPayload,
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
        data: userProfilePayload,
      }),
    });
  });

  await page.route("**/api/me/menu", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        message: "ok",
        data: [
          { id: "dashboard", label: "Dashboard", path: "/dashboard", visible: true, children: [] },
          { id: "dev", label: "Developer Tools", path: "/dev", visible: true, children: [] },
        ],
      }),
    });
  });

  await page.goto("/login");
  await page.getByLabel("Username").fill("devtools");
  await page.getByLabel("Password").fill("secret");
  await page.getByRole("button", { name: "Login" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);

  await page.goto("/dev");
  await expect(page.getByRole("heading", { name: "Developer Tools" })).toBeVisible();

  await page.getByRole("link", { name: /CRUD Demo/ }).click();

  await expect(page).toHaveURL(/\/dev\/crud-demo$/);
  await expect(page.getByRole("heading", { name: "Dev CRUD Demo" })).toBeVisible();
});
