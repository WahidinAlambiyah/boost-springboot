const FALLBACK = "-";

function toTrimmedString(value: string | null | undefined): string {
  return (value ?? "").trim();
}

function parseDateValue(value: string | null | undefined): Date | null {
  const normalized = toTrimmedString(value);

  if (!normalized) {
    return null;
  }

  const date = new Date(normalized);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date;
}

const dateFormatter = new Intl.DateTimeFormat("id-ID", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

const dateTimeFormatter = new Intl.DateTimeFormat("id-ID", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

const idrFormatter = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0,
});

export function formatDate(value: string | null | undefined): string {
  const date = parseDateValue(value);

  if (!date) {
    return FALLBACK;
  }

  return dateFormatter.format(date);
}

export function formatDateTime(value: string | null | undefined): string {
  const date = parseDateValue(value);

  if (!date) {
    return FALLBACK;
  }

  return dateTimeFormatter.format(date);
}

export function formatCurrencyIDR(value: number | string | null | undefined): string {
  if (value == null) {
    return FALLBACK;
  }

  const normalized = typeof value === "string" ? toTrimmedString(value) : value;

  if (normalized === "") {
    return FALLBACK;
  }

  const numericValue = typeof normalized === "number" ? normalized : Number(normalized);

  if (!Number.isFinite(numericValue)) {
    return FALLBACK;
  }

  return idrFormatter.format(numericValue);
}

export function formatStatusLabel(value: string | null | undefined): string {
  const normalized = toTrimmedString(value);

  if (!normalized) {
    return FALLBACK;
  }

  return normalized
    .replace(/[\s_-]+/g, " ")
    .toLowerCase()
    .split(" ")
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
