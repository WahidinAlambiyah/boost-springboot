export const TRAINING_CENTER_DEMO_STATUSES = ["ACTIVE", "INACTIVE"] as const;

export type TrainingCenterDemoStatus = (typeof TRAINING_CENTER_DEMO_STATUSES)[number];

export interface TrainingCenterDemo {
  id: string;
  code: string;
  name: string;
  location: string;
  activeStudents: number;
  coachCount: number;
  status: TrainingCenterDemoStatus;
  createdAt: string;
  updatedAt: string;
}

export type CreateTrainingCenterDemoPayload = Pick<
  TrainingCenterDemo,
  "code" | "name" | "location" | "activeStudents" | "coachCount" | "status"
>;

export type UpdateTrainingCenterDemoPayload = Partial<CreateTrainingCenterDemoPayload>;

export interface TrainingCenterDemoListParams {
  search?: string;
  status?: TrainingCenterDemoStatus | "ALL";
}

export type TrainingCenterDemoSearchParams = TrainingCenterDemoListParams;

// TODO: Replace USER_READ fallback with DEV_TOOLS_READ once the backend authority is available.
export const DEV_TOOLS_READ_PERMISSIONS = ["USER_READ"] as const;

// TODO: Replace ROLE_READ fallback with DEV_TOOLS_WRITE once the backend authority is available.
export const DEV_TOOLS_WRITE_PERMISSIONS = ["ROLE_READ"] as const;
