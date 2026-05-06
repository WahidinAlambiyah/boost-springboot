import { devCrudMockItems } from "./dev-crud.mock";
import type { DevCrudFormValues } from "./dev-crud.schema";
import type { DevCrudItem } from "./dev-crud.types";

let devCrudItems: DevCrudItem[] = [...devCrudMockItems];

const waitForMockLatency = () => new Promise((resolve) => setTimeout(resolve, 150));

const cloneItems = () => devCrudItems.map((item) => ({ ...item }));

export const devCrudService = {
  async list(): Promise<DevCrudItem[]> {
    await waitForMockLatency();
    return cloneItems();
  },

  async create(values: DevCrudFormValues): Promise<DevCrudItem> {
    await waitForMockLatency();

    const item: DevCrudItem = {
      id: `dev-crud-${Date.now()}`,
      ...values,
      createdAt: new Date().toISOString(),
    };

    devCrudItems = [item, ...devCrudItems];
    return { ...item };
  },

  async update(id: string, values: DevCrudFormValues): Promise<DevCrudItem> {
    await waitForMockLatency();

    let updated: DevCrudItem | undefined;
    devCrudItems = devCrudItems.map((item) => {
      if (item.id !== id) {
        return item;
      }

      updated = { ...item, ...values };
      return updated;
    });

    if (!updated) {
      throw new Error("Dev CRUD item tidak ditemukan");
    }

    return { ...updated };
  },

  async remove(id: string): Promise<void> {
    await waitForMockLatency();
    devCrudItems = devCrudItems.filter((item) => item.id !== id);
  },

  resetMockData(): void {
    devCrudItems = [...devCrudMockItems];
  },
};
