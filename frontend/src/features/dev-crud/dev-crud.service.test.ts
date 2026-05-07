import type { CreateTrainingCenterDemoPayload } from "./dev-crud.types";
import { devCrudService } from "./dev-crud.service";

const STORAGE_KEY = "boost.devCrud.trainingCenters";

const payload: CreateTrainingCenterDemoPayload = {
  code: "BTC-DPS",
  name: "Boost Training Center Denpasar",
  location: "Denpasar",
  activeStudents: 42,
  coachCount: 5,
  status: "ACTIVE",
};

const resolveServiceDelay = async () => {
  await vi.runAllTimersAsync();
};

describe("devCrudService", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-05-07T10:00:00.000Z"));
    window.localStorage.removeItem(STORAGE_KEY);
  });

  afterEach(() => {
    window.localStorage.removeItem(STORAGE_KEY);
    vi.useRealTimers();
  });

  it("creates a training center demo item and returns it in list", async () => {
    const createRequest = devCrudService.create(payload);
    await resolveServiceDelay();
    const created = await createRequest;

    expect(created).toEqual(
      expect.objectContaining({
        id: expect.any(String),
        ...payload,
      }),
    );

    const listRequest = devCrudService.list();
    await resolveServiceDelay();
    const items = await listRequest;

    expect(items).toEqual(expect.arrayContaining([created]));
  });

  it("updates an existing training center demo item", async () => {
    const createRequest = devCrudService.create(payload);
    await resolveServiceDelay();
    const created = await createRequest;

    vi.setSystemTime(new Date("2026-05-07T10:05:00.000Z"));

    const updateRequest = devCrudService.update(created.id, {
      name: "Boost Training Center Bali",
      status: "INACTIVE",
      activeStudents: 64,
    });
    await resolveServiceDelay();
    const updated = await updateRequest;

    expect(updated).toEqual(
      expect.objectContaining({
        id: created.id,
        name: "Boost Training Center Bali",
        status: "INACTIVE",
        activeStudents: 64,
      }),
    );

    const getByIdRequest = devCrudService.getById(created.id);
    await resolveServiceDelay();
    const stored = await getByIdRequest;

    expect(stored).toEqual(updated);
  });

  it("removes an existing training center demo item", async () => {
    const createRequest = devCrudService.create(payload);
    await resolveServiceDelay();
    const created = await createRequest;

    const removeRequest = devCrudService.remove(created.id);
    await resolveServiceDelay();
    await removeRequest;

    const listRequest = devCrudService.list();
    await resolveServiceDelay();
    const items = await listRequest;

    expect(items).not.toEqual(expect.arrayContaining([expect.objectContaining({ id: created.id })]));
  });
});
