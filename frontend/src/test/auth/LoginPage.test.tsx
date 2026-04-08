import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import LoginPage from "../../app/auth/login/page";

const { requestMock, persistSessionMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  persistSessionMock: vi.fn(),
}));

vi.mock("../../lib/api-client", () => ({
  ApiError: class ApiError extends Error {
    payload?: { message?: string };

    constructor(status: number, payload?: { message?: string }) {
      super(payload?.message ?? `Request failed: ${status}`);
      this.payload = payload;
    }
  },
  apiClient: {
    request: requestMock,
  },
}));

vi.mock("../../lib/auth-session", () => ({
  persistSession: persistSessionMock,
}));

afterEach(() => {
  requestMock.mockReset();
  persistSessionMock.mockReset();
  cleanup();
});

describe("LoginPage", () => {
  it("shows required-field validation", async () => {
    render(<LoginPage />);

    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    expect((await screen.findByRole("alert")).textContent).toContain("Email and password are required.");
    expect(requestMock).not.toHaveBeenCalled();
  });

  it("stores the session after a successful login", async () => {
    requestMock.mockResolvedValueOnce({
      accessToken: "access",
      refreshToken: "refresh",
      tokenType: "Bearer",
      expiresIn: 3600,
    });

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "customer@example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "SecurePass1!" } });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(persistSessionMock).toHaveBeenCalledWith({
        accessToken: "access",
        refreshToken: "refresh",
        tokenType: "Bearer",
        expiresIn: 3600,
      });
    });
  });

  it("renders backend authentication failures", async () => {
    requestMock.mockRejectedValueOnce({ payload: { message: "The supplied credentials are invalid." } });

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "customer@example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "WrongPass1!" } });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    expect((await screen.findByRole("alert")).textContent).toContain("The supplied credentials are invalid.");
  });
});
