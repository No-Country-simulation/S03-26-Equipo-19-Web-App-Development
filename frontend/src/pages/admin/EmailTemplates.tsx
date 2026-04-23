import { useState } from 'react';
import { Plus, Mail, Pencil, Trash2, Star } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { KpiCardSimple } from '../../components/ui/KpiCardSimple';
import { Modal } from '../../components/ui/Modal';
import { useGetTemplates } from '../../services/use_queries/templates-query';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiTemplatesService } from '../../services/general_api';
import type { TemplateResType } from '../../types/template.types';
import { EmailTemplateForm } from './../../components/email_templates/EmailTemplateForm';

const TemplateCard = ({
  template,
  onDelete,
}: {
  template: TemplateResType;
  onDelete: (id: number) => void;
}) => (
  <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
    <div>
      <div className="flex items-center gap-2 mb-3">
        {template.channel === 'EMAIL' ? (
          <Mail size={16} className="text-blue-400" />
        ) : (
          <Star size={16} className="text-yellow-400 fill-yellow-400" />
        )}
        <h3 className="text-sm font-bold text-primary">{template.name}</h3>
      </div>
      <p className="text-xs text-slate-500 leading-relaxed line-clamp-3">{template.body}</p>
    </div>
    <div className="flex items-center justify-between pt-3 border-t border-slate-100">
      <span className="text-xs text-slate-400">
        {template.updatedAt
          ? `Actualizado el ${new Date(template.updatedAt).toLocaleDateString('es-AR', { day: 'numeric', month: 'long' })}`
          : '—'}
      </span>
      <div className="flex items-center gap-3">
        <button className="text-blue-500 hover:text-blue-700 transition-colors"><Pencil size={15} /></button>
        <button
          className="text-slate-400 hover:text-red-500 transition-colors"
          onClick={() => onDelete(template.id)}
        ><Trash2 size={15} /></button>
      </div>
    </div>
  </div>
);

export const EmailTemplates = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const qc = useQueryClient();

  const { data: templates = [], isLoading, isError } = useGetTemplates();

  const deleteTemplate = useMutation({
    mutationFn: (id: number) => apiTemplatesService.delete(`/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['templates'] }),
  });

  const emailCount = templates.filter((t: TemplateResType) => t.channel === 'EMAIL').length;
  const waCount    = templates.filter((t: TemplateResType) => t.channel === 'WHATSAPP').length;

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-primary">Plantillas de Correo Electrónico</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Administra plantillas optimizadas para mejorar la comunicación y conversión
          </p>
        </div>
        <Button
          variant="primary" size="md"
          className="flex items-center gap-2 whitespace-nowrap"
          onClick={() => setModalOpen(true)}
        >
          <Plus size={16} /> Crear Plantilla
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCardSimple icon={<Mail size={22} />} label="Total de plantillas"    value={String(templates.length)} />
        <KpiCardSimple icon={<Mail size={22} />} label="Plantillas de email"    value={String(emailCount)} />
        <KpiCardSimple icon={<Mail size={22} />} label="Plantillas WhatsApp"    value={String(waCount)} />
        <KpiCardSimple icon={<Mail size={22} />} label="Promedio diario"        value="—" />
      </div>

      {isLoading ? (
        <div className="text-center py-16 text-slate-400 text-sm">Cargando plantillas...</div>
      ) : isError ? (
        <div className="text-center py-16 text-red-400 text-sm">Error al cargar plantillas</div>
      ) : templates.length === 0 ? (
        <div className="text-center py-16 text-slate-400 text-sm">No hay plantillas registradas</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {templates.map((template: TemplateResType) => (
            <TemplateCard
              key={template.id}
              template={template}
              onDelete={id => deleteTemplate.mutate(id)}
            />
          ))}
        </div>
      )}

      <div className="flex items-center justify-between mt-4">
        <p className="text-sm text-slate-500">Mostrando {templates.length} plantillas</p>
        <div className="flex items-center gap-1">
          <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 disabled:opacity-30 transition-colors text-sm">{'<'}</button>
          {[1, 2, 3].map(page => (
            <button key={page} onClick={() => setCurrentPage(page)}
              className={`w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors ${
                currentPage === page ? 'bg-primary text-white' : 'text-slate-500 hover:bg-slate-100'
              }`}>{page}</button>
          ))}
          <button onClick={() => setCurrentPage(p => p + 1)}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 transition-colors text-sm">{'>'}</button>
        </div>
      </div>

      <div className="mt-12 pt-6 border-t border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-slate-400">
        <span>© 2026 Conversa CRM. Todos los derechos reservados.</span>
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Crear Plantilla">
        <EmailTemplateForm
          onCancel={() => setModalOpen(false)}
          onSuccess={() => setModalOpen(false)}
        />
      </Modal>
    </div>
  );
};