import { siteContent } from "@/content/public/site";

const DEFAULT_WHATSAPP_NUMBER = "6280000000000";
const DEFAULT_WHATSAPP_MESSAGE = "Halo, saya ingin tanya trial class pushbike untuk anak.";

function sanitizePhoneNumber(value: string) {
  return value.replace(/[^\d]/g, "");
}

function resolveWhatsAppNumber() {
  const candidates = [
    process.env.NEXT_PUBLIC_WHATSAPP_NUMBER,
    siteContent.whatsappNumber,
    DEFAULT_WHATSAPP_NUMBER,
  ];

  for (const candidate of candidates) {
    const sanitizedPhone = sanitizePhoneNumber(candidate ?? "");

    if (sanitizedPhone) {
      return sanitizedPhone;
    }
  }

  return DEFAULT_WHATSAPP_NUMBER;
}

export function createWhatsAppLink(message = DEFAULT_WHATSAPP_MESSAGE) {
  const phone = resolveWhatsAppNumber();
  const encodedMessage = encodeURIComponent(message);
  return `https://wa.me/${phone}?text=${encodedMessage}`;
}

export { DEFAULT_WHATSAPP_MESSAGE };
