import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import CustomerDetailPage from "../../app/customers/[customerId]/page";

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

describe("CustomerDetailPage", () => {
  it("renders customer details", async () => {
    requestMock.mockResolvedValueOnce({
      customerId: 1,
      name: "Jamie Customer",
      address: "10 Main Street",
      type: "PERSON",
      createdAt: "2026-04-08T10:15:30Z",
      updatedAt: "2026-04-08T10:15:30Z",
    });

    render(
      <MemoryRouter initialEntries={["/customers/1"]}>
        <Routes>
          <Route path="/customers/:customerId" element={<CustomerDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText("Jamie Customer")).toBeTruthy();
    expect(screen.getByText("10 Main Street")).toBeTruthy();
  });
});
