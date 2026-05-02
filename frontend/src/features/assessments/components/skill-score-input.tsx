"use client";

interface SkillScoreInputProps {
  label: string;
  value: number;
  notes: string;
  maxScore?: number;
  disabled?: boolean;
  onChange: (score: number) => void;
  onNotesChange: (notes: string) => void;
}

export default function SkillScoreInput({
  label,
  value,
  notes,
  maxScore = 5,
  disabled = false,
  onChange,
  onNotesChange,
}: SkillScoreInputProps) {
  return (
    <div className="rounded-lg border border-zinc-200 p-4 sm:p-5">
      <p className="font-medium text-zinc-900">{label}</p>
      <div className="mt-3 grid grid-cols-5 gap-2 sm:flex sm:flex-wrap">
        {Array.from({ length: Math.min(maxScore, 5) }, (_, i) => i + 1).map((score) => (
          <button
            key={score}
            type="button"
            disabled={disabled}
            onClick={() => onChange(score)}
            className={`min-h-12 w-full rounded-md border text-base font-semibold sm:min-w-12 sm:w-12 sm:text-sm ${
              value === score
                ? "border-zinc-900 bg-zinc-900 text-white"
                : "border-zinc-300 bg-white text-zinc-700"
            } disabled:opacity-50`}
          >
            {score}
          </button>
        ))}
      </div>
      <textarea
        rows={2}
        value={notes}
        disabled={disabled}
        onChange={(event) => onNotesChange(event.target.value)}
        placeholder="Notes skill (opsional)"
        className="mt-3 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
      />
    </div>
  );
}
