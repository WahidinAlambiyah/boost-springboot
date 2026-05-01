"use client";

interface AttendanceSessionOption {
  id: string;
  label: string;
  subtitle?: string;
}

interface AttendanceSessionPickerProps {
  sessions: AttendanceSessionOption[];
  value: string;
  onChange: (sessionId: string) => void;
  disabled?: boolean;
}

export default function AttendanceSessionPicker({
  sessions,
  value,
  onChange,
  disabled = false,
}: AttendanceSessionPickerProps) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-zinc-700">Pilih sesi kelas</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        disabled={disabled}
        className="min-h-12 w-full rounded-md border border-zinc-300 bg-white px-4 py-3 text-base text-zinc-900 disabled:cursor-not-allowed disabled:bg-zinc-100"
      >
        <option value="">Pilih sesi</option>
        {sessions.map((session) => (
          <option key={session.id} value={session.id}>
            {session.label}
            {session.subtitle ? ` • ${session.subtitle}` : ""}
          </option>
        ))}
      </select>
    </label>
  );
}

export type { AttendanceSessionOption, AttendanceSessionPickerProps };
