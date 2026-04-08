import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import RegisterPage from "../../app/auth/register/page";

const { requestMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
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

afterEach(() => {
  requestMock.mockReset();
  cleanup();
});

describe("RegisterPage", () => {
  it("shows client-side validation errors", async () => {
    render(<RegisterPage />);

    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    expect((await screen.findByRole("alert")).textContent).toContain("Email is required.");
    expect(requestMock).not.toHaveBeenCalled();
  });

  it("submits valid registration data", async () => {
    requestMock.mockResolvedValueOnce({ userId: "123" });
    render(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "Customer@Example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "SecurePass1!" } });
    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    await waitFor(() => {
      expect(requestMock).toHaveBeenCalledWith("/auth/register", {
        method: "POST",
        body: {
          email: "customer@example.com",
          password: "SecurePass1!",
        },
      });
    });

    expect(await screen.findByText(/registration complete/i)).toBeTruthy();
  });

  it("renders server errors as plain text", async () => {
    requestMock.mockRejectedValueOnce({ payload: { message: "<script>alert('x')</script>" } });
    render(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "customer@example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "SecurePass1!" } });
    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    const alert = await screen.findByRole("alert");
    expect(alert.textContent).toContain("<script>alert('x')</script>");
    expect(alert.querySelector("script")).toBeNull();
  });
});
