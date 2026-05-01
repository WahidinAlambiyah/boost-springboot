import { api } from "@/lib/api";
import { Student } from "@/lib/api-types";
import { StudentFormValues } from "@/features/students/student.schema";
import { ApiResponse } from "@/types/api";

export class NotImplementedError extends Error {
  code: string;

  constructor(message = "NOT_IMPLEMENTED") {
    super(message);
    this.code = "NOT_IMPLEMENTED";
  }
}

const isStudentsEndpointEnabled = process.env.NEXT_PUBLIC_STUDENTS_ENDPOINT_ENABLED === "true";

const throwNotImplemented = () => {
  throw new NotImplementedError("Students endpoint belum tersedia");
};

export const studentService = {
  isEndpointEnabled: isStudentsEndpointEnabled,
  async list(): Promise<Student[]> {
    if (!isStudentsEndpointEnabled) throwNotImplemented();
    const response = await api.get<ApiResponse<Student[]>>("/api/students");
    return response.data.data;
  },
  async create(payload: StudentFormValues): Promise<ApiResponse<Student>> {
    if (!isStudentsEndpointEnabled) throwNotImplemented();
    const response = await api.post<ApiResponse<Student>>("/api/students", payload);
    return response.data;
  },
  async update(id: string, payload: StudentFormValues): Promise<ApiResponse<Student>> {
    if (!isStudentsEndpointEnabled) throwNotImplemented();
    const response = await api.put<ApiResponse<Student>>(`/api/students/${id}`, payload);
    return response.data;
  },
};
