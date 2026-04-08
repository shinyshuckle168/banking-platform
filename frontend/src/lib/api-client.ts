export type ApiErrorPayload = {
  code: string;
  message: string;
  field?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly payload?: ApiErrorPayload;

  constructor(status: number, payload?: ApiErrorPayload) {
    super(payload?.message ?? "API request failed.");
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

export type ApiClientOptions = {
  baseUrl?: string;
  getAccessToken?: () => string | null | undefined;
  fetchImpl?: typeof fetch;
};

export type RequestOptions = {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  body?: unknown;
  headers?: HeadersInit;
  signal?: AbortSignal;
};

export class ApiClient {
  private readonly baseUrl: string;
  private readonly getAccessToken?: () => string | null | undefined;
  private readonly fetchImpl: typeof fetch;

  constructor(options: ApiClientOptions = {}) {
    this.baseUrl = options.baseUrl ?? import.meta.env.VITE_API_BASE_URL ?? "/api";
    this.getAccessToken = options.getAccessToken;
    this.fetchImpl = options.fetchImpl ?? fetch;
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const headers = new Headers(options.headers);
    headers.set("Accept", "application/json");

    const token = this.getAccessToken?.();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    let body: BodyInit | undefined;
    if (options.body !== undefined) {
      headers.set("Content-Type", "application/json");
      body = JSON.stringify(options.body);
    }

    const response = await this.fetchImpl(`${this.baseUrl}${path}`, {
      method: options.method ?? "GET",
      headers,
      body,
      signal: options.signal,
    });

    if (response.status === 204) {
      return undefined as T;
    }

    const contentType = response.headers.get("content-type") ?? "";
    const isJson = contentType.includes("application/json");
    const payload = isJson ? await response.json() : undefined;

    if (!response.ok) {
      throw new ApiError(response.status, payload as ApiErrorPayload | undefined);
    }

    return payload as T;
  }
}

export const apiClient = new ApiClient();
