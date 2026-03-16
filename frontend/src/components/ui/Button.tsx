import React from "react";
import { cn } from "../../lib/utils";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "ghost";
  size?: "sm" | "md" | "lg";
  children: React.ReactNode;
}

export function Button({
  variant = "primary",
  size = "md",
  className,
  children,
  ...props
}: ButtonProps) {
  const baseStyles =
    "inline-flex items-center justify-center font-bold rounded-2xl transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed";

  const variants = {
    primary:
      "bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-500 text-white shadow-lg shadow-indigo-200 hover:shadow-2xl hover:shadow-indigo-300 hover:scale-[1.02]",
    secondary:
      "bg-white/10 border border-white/20 text-white backdrop-blur-sm hover:bg-white/20",
    outline:
      "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300",
    ghost: "text-slate-500 hover:bg-slate-50 hover:text-slate-900",
  };

  const sizes = {
    sm: "px-4 py-2 text-sm",
    md: "px-6 py-3 text-base",
    lg: "px-10 py-5 text-lg",
  };

  return (
    <button
      className={cn(baseStyles, variants[variant], sizes[size], className)}
      {...props}
    >
      {children}
    </button>
  );
}
