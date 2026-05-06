import { afterEach, describe, expect, it } from "vitest";

import { createWhatsAppLink } from "./whatsapp";

const originalWhatsAppNumber = process.env.NEXT_PUBLIC_WHATSAPP_NUMBER;

describe("createWhatsAppLink", () => {
  afterEach(() => {
    if (originalWhatsAppNumber === undefined) {
      delete process.env.NEXT_PUBLIC_WHATSAPP_NUMBER;
      return;
    }

    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = originalWhatsAppNumber;
  });

  it("falls back to the default number when the sanitized env number is empty", () => {
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = " +() - ";

    expect(createWhatsAppLink()).toBe(
      "https://wa.me/6280000000000?text=Halo%2C%20saya%20ingin%20tanya%20trial%20class%20pushbike%20untuk%20anak.",
    );
  });

  it("sanitizes the configured env number before building the URL", () => {
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER = "+62 812-3456-7890";

    expect(createWhatsAppLink("Halo admin")).toBe("https://wa.me/6281234567890?text=Halo%20admin");
  });
});
