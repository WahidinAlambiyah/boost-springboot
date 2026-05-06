import { afterEach, describe, expect, it, vi } from "vitest";

import { siteContent } from "@/content/public/site";

import { createWhatsAppLink } from "./whatsapp";

const originalWhatsAppNumber = process.env.NEXT_PUBLIC_WHATSAPP_NUMBER;

describe("createWhatsAppLink", () => {
  afterEach(() => {
    vi.doUnmock("@/content/public/site");
    vi.resetModules();

    if (originalWhatsAppNumber === undefined) {
      delete process.env.NEXT_PUBLIC_WHATSAPP_NUMBER;
      return;
    }

    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = originalWhatsAppNumber;
  });

  it("falls back to the site content number when the sanitized env number is empty", () => {
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = " +() - ";

    expect(createWhatsAppLink()).toBe(
      `https://wa.me/${siteContent.whatsappNumber}?text=Halo%2C%20saya%20ingin%20tanya%20trial%20class%20pushbike%20untuk%20anak.`,
    );
  });

  it("uses the site content number when the env number is not configured", () => {
    delete process.env.NEXT_PUBLIC_WHATSAPP_NUMBER;

    expect(createWhatsAppLink("Halo admin")).toBe(
      `https://wa.me/${siteContent.whatsappNumber}?text=Halo%20admin`,
    );
  });

  it("falls back to the internal dummy number when env and site content are empty after sanitizing", async () => {
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = " +() - ";
    vi.doMock("@/content/public/site", () => ({
      siteContent: {
        whatsappNumber: " +() - ",
      },
    }));

    const { createWhatsAppLink: createMockedWhatsAppLink } = await import("./whatsapp");

    expect(createMockedWhatsAppLink("Halo admin")).toBe("https://wa.me/6280000000000?text=Halo%20admin");
  });

  it("sanitizes the configured env number before building the URL", () => {
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = "+62 812-3456-7890";

    expect(createWhatsAppLink("Halo admin")).toBe("https://wa.me/6281234567890?text=Halo%20admin");
  });
});
