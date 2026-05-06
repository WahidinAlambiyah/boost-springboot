import { devCrudMockItems } from "./dev-crud.mock";
import type {
  CreateTrainingCenterDemoPayload,
  TrainingCenterDemo,
  TrainingCenterDemoListParams,
  UpdateTrainingCenterDemoPayload,
} from "./dev-crud.types";

const STORAGE_KEY = "boost.devCrud.trainingCenters";
const MIN_DELAY_MS = 200;
const MAX_DELAY_MS = 500;

let memoryItems: TrainingCenterDemo[] = devCrudMockItems.map((item) => ({ ...item }));

export const delay = () =>
  new Promise((resolve) => {
    const timeout = Math.floor(Math.random() * (MAX_DELAY_MS - MIN_DELAY_MS + 1)) + MIN_DELAY_MS;
    setTimeout(resolve, timeout);
  });

const cloneItems = (items: TrainingCenterDemo[]) => items.map((item) => ({ ...item }));
const isBrowser = () => typeof window !== "undefined";

const persistItems = (items: TrainingCenterDemo[]) => {
  memoryItems = cloneItems(items);

  if (isBrowser()) {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }
};

const readItems = () => {
  if (!isBrowser()) {
    return cloneItems(memoryItems);
  }

  const stored = window.localStorage.getItem(STORAGE_KEY);

  if (!stored) {
    persistItems(devCrudMockItems);
    return cloneItems(devCrudMockItems);
  }

  try {
    const parsed = JSON.parse(stored) as TrainingCenterDemo[];
    memoryItems = cloneItems(parsed);
    return cloneItems(parsed);
  } catch {
    persistItems(devCrudMockItems);
    return cloneItems(devCrudMockItems);
  }
};

const normalizeSearch = (value?: string) => value?.trim().toLowerCase();

const applyParams = (items: TrainingCenterDemo[], params?: TrainingCenterDemoListParams) => {
  const search = normalizeSearch(params?.search);

  return items.filter((item) => {
    const matchesStatus = !params?.status || params.status === "ALL" || item.status === params.status;
    const matchesSearch =
      !search ||
      [item.code, item.name, item.location].some((value) => value.toLowerCase().includes(search));

    return matchesStatus && matchesSearch;
  });
};

export const devCrudService = {
  async list(params?: TrainingCenterDemoListParams): Promise<TrainingCenterDemo[]> {
    await delay();
    return applyParams(readItems(), params);
  },

  async getById(id: string): Promise<TrainingCenterDemo> {
    await delay();

    const item = readItems().find((entry) => entry.id === id);

    if (!item) {
      throw new Error("Training center demo tidak ditemukan");
    }

    return { ...item };
  },

  async create(payload: CreateTrainingCenterDemoPayload): Promise<TrainingCenterDemo> {
    await delay();

    const now = new Date().toISOString();
    const item: TrainingCenterDemo = {
      id: `training-center-demo-${Date.now()}`,
      ...payload,
      createdAt: now,
      updatedAt: now,
    };

    persistItems([item, ...readItems()]);
    return { ...item };
  },

  async update(id: string, payload: UpdateTrainingCenterDemoPayload): Promise<TrainingCenterDemo> {
    await delay();

    let updated: TrainingCenterDemo | undefined;
    const items = readItems().map((item) => {
      if (item.id !== id) {
        return item;
      }

      updated = { ...item, ...payload, updatedAt: new Date().toISOString() };
      return updated;
    });

    if (!updated) {
      throw new Error("Training center demo tidak ditemukan");
    }

    persistItems(items);
    return { ...updated };
  },

  async remove(id: string): Promise<void> {
    await delay();

    const items = readItems();
    const nextItems = items.filter((item) => item.id !== id);

    if (nextItems.length === items.length) {
      throw new Error("Training center demo tidak ditemukan");
    }

    persistItems(nextItems);
  },

  resetMockData(): void {
    persistItems(devCrudMockItems);
  },
};
