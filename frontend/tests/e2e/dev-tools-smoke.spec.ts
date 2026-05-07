import { expect, type Page, test } from "@playwright/test";

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
  permissions: ["USER_READ", "ROLE_READ"],
  createdAt: "2026-01-01T00:00:00.000Z",
};

const mockDevToolsAuth = async (page: Page) => {
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
};

const loginAsDevToolsUser = async (page: Page) => {
  await page.goto("/login");
  await page.getByLabel("Username").fill("devtools");
  await page.getByLabel("Password").fill("secret");
  await page.getByRole("button", { name: "Login" }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
};

test("smoke: fallback dev tools permission opens CRUD demo", async ({ page }) => {
  await mockDevToolsAuth(page);
  await loginAsDevToolsUser(page);

  await page.goto("/dev");
  await expect(page.getByRole("heading", { name: "Developer Tools" })).toBeVisible();

  await page.getByRole("link", { name: /CRUD Demo/ }).click();

  await expect(page).toHaveURL(/\/dev\/crud-demo$/);
  await expect(page.getByRole("heading", { name: "Dev CRUD Demo" })).toBeVisible();
});

test("smoke: dummy CRUD can create, update, and delete a training center", async ({ page }) => {
  await mockDevToolsAuth(page);
  await loginAsDevToolsUser(page);

  await page.goto("/dev/crud-demo");
  await expect(page.getByRole("heading", { name: "Dev CRUD Demo" })).toBeVisible();
  await expect(page.getByText("Boost Training Center Jakarta")).toBeVisible();

  await page.getByLabel("Kode").fill("BTC-QA");
  await page.getByLabel("Nama Training Center").fill("Boost Training Center QA");
  await page.getByLabel("Lokasi").fill("QA Lab");
  await page.getByLabel("Murid Aktif").fill("11");
  await page.getByLabel("Jumlah Coach").fill("2");
  await page.locator("#dev-crud-status").selectOption("ACTIVE");
  await page.getByRole("button", { name: "Tambah training center", exact: true }).click();

  await expect(page.getByText("Boost Training Center QA")).toBeVisible();
  await expect(page.getByRole("cell", { name: "BTC-QA" })).toBeVisible();

  const createdRow = page.getByRole("row", { name: /BTC-QA Boost Training Center QA QA Lab/ });
  await createdRow.getByRole("button", { name: "Edit" }).click();
  await expect(page.getByRole("heading", { name: "Edit Training Center" })).toBeVisible();

  await page.getByLabel("Nama Training Center").fill("Boost Training Center QA Updated");
  await page.getByLabel("Murid Aktif").fill("18");
  await page.locator("#dev-crud-status").selectOption("INACTIVE");
  await page.getByRole("button", { name: "Update training center" }).click();

  await expect(page.getByText("Boost Training Center QA Updated")).toBeVisible();
  await expect(page.getByRole("cell", { name: "18" })).toBeVisible();

  page.once("dialog", (dialog) => dialog.accept());
  const updatedRow = page.getByRole("row", { name: /BTC-QA Boost Training Center QA Updated QA Lab/ });
  await updatedRow.getByRole("button", { name: "Hapus" }).click();

  await expect(page.getByText("Boost Training Center QA Updated")).toBeHidden();
});
