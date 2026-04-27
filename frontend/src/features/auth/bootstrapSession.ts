import { api, refreshAccessToken } from "@/lib/api";
import { useAuthStore } from "@/store/auth";
import { ApiResponse, UserProfileResponse } from "@/types/api";

export const bootstrapSession = async (): Promise<boolean> => {
  useAuthStore.setState({ status: "loading" });

  const { hydrateSession, clearSession } = useAuthStore.getState();
  const refreshed = await refreshAccessToken();

  if (!refreshed) {
    clearSession();
    return false;
  }

  try {
    const profileResponse = await api.get<ApiResponse<UserProfileResponse>>("/api/users/me");
    hydrateSession(profileResponse.data.data);
    return true;
  } catch {
    clearSession();
    return false;
  }
};
