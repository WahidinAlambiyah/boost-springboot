import { authService } from "@/features/auth/auth.service";
import { useAuthStore } from "@/store/auth";

export const bootstrapSession = async (): Promise<boolean> => {
  useAuthStore.setState({ status: "loading" });

  const { hydrateSession, clearSession } = useAuthStore.getState();
  const refreshed = await authService.refresh();

  if (!refreshed) {
    clearSession();
    return false;
  }

  try {
    const profile = await authService.me();
    const menu = await authService.menu();
    hydrateSession(profile, undefined, menu);
    return true;
  } catch {
    clearSession();
    return false;
  }
};
