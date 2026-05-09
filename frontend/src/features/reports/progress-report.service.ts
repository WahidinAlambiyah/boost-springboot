import { studentProgressReportService } from "./student-progress-report.service";
import type {
  StudentProgressReportParams,
  StudentProgressReportResponse,
} from "./student-progress-report.types";

export type {
  StudentProgressReportParams,
  StudentProgressReportResponse,
} from "./student-progress-report.types";

export interface StudentProgressReportFilter extends StudentProgressReportParams {
  studentId: string;
}

export const progressReportService = {
  async getStudentProgressReport(filter: StudentProgressReportFilter): Promise<StudentProgressReportResponse> {
    return studentProgressReportService.getStudentProgressReport(filter.studentId, {
      from: filter.from,
      to: filter.to,
    });
  },
};
