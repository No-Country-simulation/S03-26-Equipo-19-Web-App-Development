import { apiExportService } from "../general_api";
import type { ExportRequest } from "../../types/admin.types";

export const exportData = async (data: ExportRequest): Promise<Blob> => {
  const res = await apiExportService.post("", data, {
    responseType: "blob",
  });
  return res.data;
};

// Descarga el blob como archivo
export const downloadBlob = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
};