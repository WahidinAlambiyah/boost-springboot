import type { TrainingCenterDemo } from "./dev-crud.types";

export const devCrudMockItems: TrainingCenterDemo[] = [
  {
    id: "training-center-demo-001",
    code: "BTC-JKT",
    name: "Boost Training Center Jakarta",
    location: "Jakarta Selatan",
    activeStudents: 128,
    coachCount: 12,
    status: "ACTIVE",
    createdAt: "2026-05-01T08:00:00.000Z",
    updatedAt: "2026-05-01T08:00:00.000Z",
  },
  {
    id: "training-center-demo-002",
    code: "BTC-BDG",
    name: "Boost Training Center Bandung",
    location: "Bandung",
    activeStudents: 96,
    coachCount: 9,
    status: "ACTIVE",
    createdAt: "2026-05-02T09:30:00.000Z",
    updatedAt: "2026-05-02T09:30:00.000Z",
  },
  {
    id: "training-center-demo-003",
    code: "BTC-SBY",
    name: "Boost Training Center Surabaya",
    location: "Surabaya Barat",
    activeStudents: 0,
    coachCount: 4,
    status: "INACTIVE",
    createdAt: "2026-05-03T11:15:00.000Z",
    updatedAt: "2026-05-03T11:15:00.000Z",
  },
];
