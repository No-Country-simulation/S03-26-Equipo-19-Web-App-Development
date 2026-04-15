interface ResponseBarProps {
  rate: number;
}

export const ResponseBar = ({ rate }: ResponseBarProps) => (
  <div className="flex items-center gap-2">
    <div className="w-20 bg-slate-100 rounded-full h-1.5">
      <div
        className={`h-1.5 rounded-full ${
          rate >= 70 ? 'bg-green-500' : rate >= 50 ? 'bg-yellow-400' : 'bg-red-400'
        }`}
        style={{ width: `${rate}%` }}
      />
    </div>
    <span className="text-xs text-slate-600">{rate}%</span>
  </div>
);