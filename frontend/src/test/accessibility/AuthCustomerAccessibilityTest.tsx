import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import LoginPage from "../../app/auth/login/page";
import RegisterPage from "../../app/auth/register/page";
import CreateCustomerPage from "../../app/customers/new/page";

vi.mock("../../lib/auth-session", () => ({
  authenticatedApiClient: {
    request: vi.fn().mockResolvedValue({}),
  },
  persistSession: vi.fn(),
}));

vi.mock("../../lib/api-client", async () => {
  const actual = await vi.importActual<typeof import("../../lib/api-client")>("../../lib/api-client");
  return {
    ...actual,
    apiClient: {
      request: vi.fn().mockResolvedValue({}),
    },
  };
});

describe("AuthCustomerAccessibilityTest", () => {
  it("renders accessible labels and actions for auth screens", () => {
    render(
      <MemoryRouter initialEntries={["/auth/register"]}>
        <Routes>
          <Route path="/auth/register" element={<RegisterPage />} />
          <Route path="/auth/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(screen.getByLabelText(/email/i)).toBeTruthy();
    expect(screen.getByLabelText(/password/i)).toBeTruthy();
    expect(screen.getByRole("button", { name: /create account/i })).toBeTruthy();
  });

  it("renders accessible fields for customer creation", () => {
    render(<CreateCustomerPage />);

    expect(screen.getByLabelText(/name/i)).toBeTruthy();
    expect(screen.getByLabelText(/address/i)).toBeTruthy();
    expect(screen.getByLabelText(/type/i)).toBeTruthy();
    expect(screen.getByRole("button", { name: /create customer/i })).toBeTruthy();
  });
});
