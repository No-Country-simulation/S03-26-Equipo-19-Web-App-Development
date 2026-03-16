import React from "react";
import { cn } from "../../lib/utils";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export function Input({ label, error, className, ...props }: InputProps) {
  return (
    <div className="space-y-2">
      {label && (
        <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest">
          {label}
        </label>
      )}
      <input
        className={cn(
          "w-full bg-slate-50 border-2 border-slate-100 rounded-2xl px-5 py-4 outline-none",
          "focus:border-indigo-500 focus:bg-white transition-all font-semibold text-base",
          error && "border-red-500 focus:border-red-500",
          className,
        )}
        {...props}
      />
      {error && <p className="text-red-500 text-xs font-medium">{error}</p>}
    </div>
  );
}

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  options: { value: string; label: string }[];
}

export function Select({ label, options, className, ...props }: SelectProps) {
  return (
    <div className="space-y-2">
      {label && (
        <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest">
          {label}
        </label>
      )}
      <select
        className={cn(
          "w-full bg-slate-50 border-2 border-slate-100 rounded-2xl px-5 py-4 outline-none",
          "focus:border-indigo-500 focus:bg-white transition-all font-bold text-sm cursor-pointer",
          className,
        )}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
}
