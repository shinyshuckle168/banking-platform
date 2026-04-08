import { describe, expect, it } from "vitest";
import fs from "node:fs/promises";

describe("PublicEnvSafetyTest", () => {
  it("only exposes publishable frontend environment variables", async () => {
    const envTemplate = await fs.readFile(new URL("../../../../.env.example", import.meta.url), "utf8");

    expect(envTemplate).toContain("VITE_API_BASE_URL=");
    expect(envTemplate).not.toContain("VITE_JWT_SECRET");
    expect(envTemplate).not.toContain("VITE_DB_PASSWORD");
  });

  it("frontend source does not reference backend secret environment variables", async () => {
    const source = await fs.readFile(new URL("../../lib/api-client.ts", import.meta.url), "utf8");

    expect(source).not.toContain("JWT_SECRET");
    expect(source).not.toContain("DB_PASSWORD");
  });
});
