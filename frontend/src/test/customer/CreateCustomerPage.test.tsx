import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import CreateCustomerPage from "../../app/customers/new/page";

const { requestMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
}));

vi.mock("../../lib/auth-session", () => ({
  authenticatedApiClient: {
    request: requestMock,
  },
}));

afterEach(() => {
  requestMock.mockReset();
  cleanup();
});

describe("CreateCustomerPage", () => {
  it("shows required-field validation", async () => {
    render(<CreateCustomerPage />);

    fireEvent.click(screen.getByRole("button", { name: /create customer/i }));

    expect((await screen.findByRole("alert")).textContent).toContain("Name and address are required.");
  });

  it("submits a customer creation request", async () => {
    requestMock.mockResolvedValueOnce({ customerId: 1 });
    render(<CreateCustomerPage />);

    fireEvent.change(screen.getByLabelText(/name/i), { target: { value: "Jamie Customer" } });
    fireEvent.change(screen.getByLabelText(/address/i), { target: { value: "10 Main Street" } });
    fireEvent.change(screen.getByLabelText(/type/i), { target: { value: "COMPANY" } });
    fireEvent.click(screen.getByRole("button", { name: /create customer/i }));

    await waitFor(() => {
      expect(requestMock).toHaveBeenCalledWith("/customers", {
        method: "POST",
        body: {
          name: "Jamie Customer",
          address: "10 Main Street",
          type: "COMPANY",
        },
      });
    });
  });
});
