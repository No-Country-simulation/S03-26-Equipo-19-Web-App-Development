interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  containerClassName?: string;
}

export const Input = ({
  label,
  error,
  className = '',
  containerClassName = '',
  ...props
}: InputProps) => {
  return (
    <div className={`flex flex-col gap-1 ${containerClassName}`}>
      {label && (
        <label className="text-sm text-neutro-1 font-medium">{label}</label>
      )}
      <input
        className={`
          bg-white border border-neutro-2 text-neutro-1
          rounded-xl px-4 py-2.5 text-sm
          focus:outline-none focus:border-secondary
          placeholder:text-neutro-2
          disabled:opacity-50 disabled:cursor-not-allowed
          ${error ? 'border-red-500' : ''}
          ${className}
        `}
        {...props}
      />
      {error && <span className="text-xs text-red-400">{error}</span>}
    </div>
  );
};