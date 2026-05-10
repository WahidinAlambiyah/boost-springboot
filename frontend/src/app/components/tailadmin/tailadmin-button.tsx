import { type ButtonHTMLAttributes, type ReactNode } from "react";

type TailAdminButtonVariant = "primary" | "secondary" | "ghost" | "danger";
type TailAdminButtonSize = "sm" | "md";

interface TailAdminButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: TailAdminButtonVariant;
  size?: TailAdminButtonSize;
}

const variantClassName: Record<TailAdminButtonVariant, string> = {
  primary: "bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-300 disabled:bg-blue-300",
  secondary: "border border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50 focus:ring-zinc-300 disabled:text-zinc-400",
  ghost: "bg-transparent text-zinc-600 hover:bg-zinc-100 focus:ring-zinc-300 disabled:text-zinc-400",
  danger: "bg-rose-600 text-white hover:bg-rose-700 focus:ring-rose-300 disabled:bg-rose-300",
};

const sizeClassName: Record<TailAdminButtonSize, string> = {
  sm: "min-h-9 px-3 py-2 text-xs",
  md: "min-h-10 px-4 py-2.5 text-sm",
};

export function TailAdminButton({
  children,
  variant = "primary",
  size = "md",
  className = "",
  type = "button",
  ...props
}: TailAdminButtonProps) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center rounded-lg font-medium shadow-sm transition focus:outline-none focus:ring-2 disabled:cursor-not-allowed ${variantClassName[variant]} ${sizeClassName[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
