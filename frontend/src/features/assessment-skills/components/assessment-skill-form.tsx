"use client";

import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { AssessmentSkill } from "@/lib/api-types";
import {
  AssessmentSkillFormInputValues,
  AssessmentSkillFormValues,
  assessmentSkillFormSchema,
} from "@/features/assessment-skills/assessment-skill.schema";

interface AssessmentSkillFormProps {
  initialData?: AssessmentSkill | null;
  canWrite: boolean;
  isSubmitting?: boolean;
  onSubmit: (values: AssessmentSkillFormValues) => void;
  onCancelEdit?: () => void;
}

export default function AssessmentSkillForm({
  initialData,
  canWrite,
  isSubmitting = false,
  onSubmit,
  onCancelEdit,
}: AssessmentSkillFormProps) {
  const form = useForm<AssessmentSkillFormInputValues, unknown, AssessmentSkillFormValues>({
    resolver: zodResolver(assessmentSkillFormSchema),
    defaultValues: {
      code: "",
      name: "",
      description: "",
      maxScore: 1,
      orderNo: 0,
      isActive: true,
    },
  });

  useEffect(() => {
    form.reset({
      code: initialData?.code ?? "",
      name: initialData?.name ?? "",
      description: initialData?.description ?? "",
      maxScore: initialData?.maxScore ?? 1,
      orderNo: initialData?.orderNo ?? 0,
      isActive: initialData?.active ?? true,
    });
  }, [form, initialData]);

  return (
    <form className="space-y-3" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Code</span>
          <input
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            disabled={!canWrite || isSubmitting || Boolean(initialData)}
            {...form.register("code")}
          />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.code?.message}</p>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Name</span>
          <input
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            disabled={!canWrite || isSubmitting}
            {...form.register("name")}
          />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.name?.message}</p>
        </label>
      </div>

      <label className="block text-sm">
        <span className="mb-1 block font-medium text-zinc-700">Description</span>
        <textarea
          rows={3}
          className="w-full rounded-md border border-zinc-300 px-3 py-2"
          disabled={!canWrite || isSubmitting}
          {...form.register("description")}
        />
        <p className="mt-1 text-xs text-red-600">{form.formState.errors.description?.message}</p>
      </label>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Max Score</span>
          <input
            type="number"
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            disabled={!canWrite || isSubmitting}
            {...form.register("maxScore")}
          />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.maxScore?.message}</p>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-zinc-700">Order No</span>
          <input
            type="number"
            className="w-full rounded-md border border-zinc-300 px-3 py-2"
            disabled={!canWrite || isSubmitting}
            {...form.register("orderNo")}
          />
          <p className="mt-1 text-xs text-red-600">{form.formState.errors.orderNo?.message}</p>
        </label>
      </div>

      <label className="inline-flex items-center gap-2 text-sm text-zinc-700">
        <input type="checkbox" disabled={!canWrite || isSubmitting} {...form.register("isActive")} />
        Active
      </label>

      {canWrite ? (
        <div className="flex gap-2">
          <button
            type="submit"
            className="rounded-md bg-zinc-900 px-4 py-2 text-sm text-white disabled:opacity-50"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Menyimpan..." : initialData ? "Update Skill" : "Tambah Skill"}
          </button>
          {initialData && onCancelEdit ? (
            <button
              type="button"
              className="rounded-md border border-zinc-300 px-4 py-2 text-sm"
              onClick={onCancelEdit}
            >
              Batal Edit
            </button>
          ) : null}
        </div>
      ) : null}
    </form>
  );
}
