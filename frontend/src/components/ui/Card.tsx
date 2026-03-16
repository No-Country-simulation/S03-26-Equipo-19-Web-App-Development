import React from "react";
import { cn } from "../../lib/utils";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  hover?: boolean;
}

export function Card({
  children,
  className,
  hover = false,
  ...props
}: CardProps) {
  return (
    <div
      className={cn(
        "bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm",
        hover &&
          "hover:shadow-lg hover:-translate-y-1 transition-all duration-300",
        className,
      )}
      {...props}
    >
      {children}
    </div>
  );
}
