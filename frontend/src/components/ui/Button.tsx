import * as React from "react";
import { Slot } from "@radix-ui/react-slot";

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  asChild?: boolean; // 🔥 clave
}

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-primary hover:bg-secondary text-white',
  secondary: 'bg-accent hover:bg-secondary text-white',
  outline: "",
  ghost: "border border-primary text-primary hover:bg-secondary hover:text-white hover:border-none",
  danger: 'bg-error hover:bg-red-600 text-white',
};

const sizes: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs',
  md: 'px-5 py-2.5 text-sm',
  lg: 'px-6 py-3 text-base',
};

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({
    variant = 'primary',
    size = 'md',
    loading = false,
    disabled,
    children,
    className = '',
    asChild = false,
    ...props
  }, ref) => {

    const Comp = asChild ? Slot : "button";

    return (
      <Comp
        ref={ref}
        disabled={disabled || loading}
        className={`
          text-center rounded-xl font-semibold transition-colors shadow-sm
          disabled:opacity-50 disabled:cursor-not-allowed
          ${variants[variant]} ${sizes[size]} ${className}
        `}
        {...props}
      >
        {loading ? 'Cargando...' : children}
      </Comp>
    );
  }
);

Button.displayName = "Button";