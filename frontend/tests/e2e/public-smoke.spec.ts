import { expect, test } from "@playwright/test";

test("smoke: public pages render core headings", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("link", { name: "Boost Academy" })).toBeVisible();
  await expect(
    page.getByRole("heading", {
      name: "Tempat Anak Belajar Berani, Fokus, dan Percaya Diri Lewat Kelas Pushbike",
    }),
  ).toBeVisible();

  await page.goto("/program/pushbike");
  await expect(page.getByRole("heading", { name: "Program Pushbike" })).toBeVisible();

  await page.goto("/events");
  await expect(page.getByRole("heading", { name: "Events" })).toBeVisible();

  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
});

test("protected pages redirect unauthenticated users to login", async ({ page, context }) => {
  await context.clearCookies();
  await page.addInitScript(() => {
    window.localStorage.clear();
    window.sessionStorage.clear();
  });

  await page.goto("/dashboard");

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
});
