export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface UserProfileResponse {
  id: string;
  username: string;
  email: string;
  isActive: boolean;
  roles: string[];
  permissions: string[];
  createdAt: string;
}

export interface ErrorResponse {
  status?: number;
  message?: string;
  data?: {
    errors?: Record<string, string>;
  };
}
