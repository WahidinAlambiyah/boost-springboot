import type { DevCrudItem } from "./dev-crud.types";

export const devCrudMockItems: DevCrudItem[] = [
  {
    id: "dev-crud-001",
    title: "Audit reusable table states",
    owner: "Platform Team",
    status: "ACTIVE",
    priority: "HIGH",
    dueDate: "2026-05-15",
    createdAt: "2026-05-01T08:00:00.000Z",
  },
  {
    id: "dev-crud-002",
    title: "Document form validation patterns",
    owner: "Frontend Guild",
    status: "DRAFT",
    priority: "MEDIUM",
    dueDate: "2026-05-20",
    createdAt: "2026-05-02T09:30:00.000Z",
  },
  {
    id: "dev-crud-003",
    title: "Archive deprecated component variants",
    owner: "Design System",
    status: "ARCHIVED",
    priority: "LOW",
    dueDate: "2026-05-30",
    createdAt: "2026-05-03T11:15:00.000Z",
  },
];
