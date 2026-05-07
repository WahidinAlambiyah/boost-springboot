import { expect, test } from "@playwright/test";

test.describe("public route smoke tests", () => {
  test("homepage renders the public landing page", async ({ page }) => {
    await page.goto("/");

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByRole("link", { name: "Boost Academy" })).toBeVisible();
    await expect(
      page.getByRole("heading", {
        name: "Tempat Anak Belajar Berani, Fokus, dan Percaya Diri Lewat Kelas Pushbike",
      }),
    ).toBeVisible();
  });

  test("pushbike program page renders the program detail", async ({ page }) => {
    await page.goto("/program/pushbike");

    await expect(page).toHaveURL(/\/program\/pushbike$/);
    await expect(page.getByRole("heading", { name: "Program Pushbike" })).toBeVisible();
  });

  test("events page renders the event listings", async ({ page }) => {
    await page.goto("/events");

    await expect(page).toHaveURL(/\/events$/);
    await expect(page.getByRole("heading", { name: "Events" })).toBeVisible();
  });

  test("login page renders the login form", async ({ page }) => {
    await page.goto("/login");

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
    await expect(page.getByLabel("Username")).toBeVisible();
    await expect(page.getByLabel("Password")).toBeVisible();
    await expect(page.getByRole("button", { name: "Login" })).toBeVisible();
  });
});

test("protected dashboard redirects unauthenticated users to login", async ({ page, context }) => {
  await context.clearCookies();
  await page.addInitScript(() => {
    window.localStorage.clear();
    window.sessionStorage.clear();
  });

  await page.goto("/dashboard");

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
});
