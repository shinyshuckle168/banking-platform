import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import EditCustomerPage from "../../app/customers/[customerId]/edit/page";

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

describe("EditCustomerPage", () => {
  it("loads customer data and shows conflict errors", async () => {
    requestMock
      .mockResolvedValueOnce({
        customerId: 1,
        name: "Jamie Customer",
        address: "10 Main Street",
        type: "PERSON",
        updatedAt: "2026-04-08T10:15:30Z",
      })
      .mockRejectedValueOnce({ payload: { message: "Customer state is stale. Refresh and try again." } });

    render(
      <MemoryRouter initialEntries={["/customers/1/edit"]}>
        <Routes>
          <Route path="/customers/:customerId/edit" element={<EditCustomerPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByDisplayValue("Jamie Customer")).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: /save changes/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert").textContent).toContain("Customer state is stale. Refresh and try again.");
    });
  });
});
