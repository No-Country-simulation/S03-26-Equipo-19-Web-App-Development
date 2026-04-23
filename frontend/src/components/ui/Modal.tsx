interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
}

export const Modal = ({ isOpen, onClose, title, children }: ModalProps) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-primary/60 backdrop-blur-sm"
        onClick={onClose}
      />
      {/* Panel */}
      <div className="relative z-10 bg-white border border-gray-700 rounded-2xl shadow-xl w-full max-w-md mx-4 p-10">
        <div className="flex justify-between items-center mb-2">
          <h2 className="text-secondary font-semibold text-lg">{title}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-primary transition-colors text-xl leading-none"
          >
            ✕
          </button>
        </div>

        <hr className="border-neutro-2 mb-6" />
        {children}
      </div>
    </div>
  );
};