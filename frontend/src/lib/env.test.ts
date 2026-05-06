import { getApiBaseUrl } from "@/lib/env";

const originalApiUrl = process.env.NEXT_PUBLIC_API_URL;
const originalApiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
const originalLocation = window.location;

const setLocation = (url: string) => {
  Object.defineProperty(window, "location", {
    configurable: true,
    value: new URL(url),
  });
};

describe("getApiBaseUrl", () => {
  beforeEach(() => {
    process.env.NEXT_PUBLIC_API_URL = originalApiUrl;
    process.env.NEXT_PUBLIC_API_BASE_URL = originalApiBaseUrl;
    setLocation("http://localhost:3000/login");
  });

  afterAll(() => {
    process.env.NEXT_PUBLIC_API_URL = originalApiUrl;
    process.env.NEXT_PUBLIC_API_BASE_URL = originalApiBaseUrl;
    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("keeps localhost API URL for local frontend development", () => {
    process.env.NEXT_PUBLIC_API_URL = "http://localhost:8123";

    expect(getApiBaseUrl()).toBe("http://localhost:8123");
  });

  it("uses configured non-local API URL on unknown deployed frontend hosts", () => {
    process.env.NEXT_PUBLIC_API_URL = "https://api.example.com";
    setLocation("https://preview.example.com/login");

    expect(getApiBaseUrl()).toBe("https://api.example.com");
  });

  it("uses the same-origin API proxy on protofeone deployment even when a localhost API URL was baked into the bundle", () => {
    process.env.NEXT_PUBLIC_API_URL = "http://localhost:8123";
    setLocation("https://protofeone.alambiyah.com/login");

    expect(getApiBaseUrl()).toBe("");
  });

  it("uses the same-origin API proxy on protofeone deployment when public API env is missing", () => {
    delete process.env.NEXT_PUBLIC_API_URL;
    delete process.env.NEXT_PUBLIC_API_BASE_URL;
    setLocation("https://protofeone.alambiyah.com/login");

    expect(getApiBaseUrl()).toBe("");
  });
});
