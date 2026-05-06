const DEFAULT_WHATSAPP_NUMBER = "6280000000000";
const DEFAULT_WHATSAPP_MESSAGE = "Halo, saya ingin tanya trial class pushbike untuk anak.";

function sanitizePhoneNumber(value: string) {
  return value.replace(/[^\d]/g, "");
}

export function createWhatsAppLink(message = DEFAULT_WHATSAPP_MESSAGE) {
  const sanitizedPhone = sanitizePhoneNumber(process.env.NEXT_PUBLIC_WHATSAPP_NUMBER ?? "");
  const phone = sanitizedPhone || DEFAULT_WHATSAPP_NUMBER;
  const encodedMessage = encodeURIComponent(message);
  return `https://wa.me/${phone}?text=${encodedMessage}`;
}

export { DEFAULT_WHATSAPP_MESSAGE };
