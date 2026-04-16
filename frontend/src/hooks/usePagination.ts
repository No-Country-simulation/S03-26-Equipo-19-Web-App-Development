import { useMemo, useState } from "react";

interface UsePaginationProps<T> {
  data: T[];
  pageSize?: number;
}

export const usePagination = <T>({
  data,
  pageSize = 10,
}: UsePaginationProps<T>) => {
  const [page, setPage] = useState(1);

  const totalPages = Math.ceil(data.length / pageSize);

  const paginatedData = useMemo(() => {
    const start = (page - 1) * pageSize;
    return data.slice(start, start + pageSize);
  }, [data, page, pageSize]);

  const nextPage = () => {
    setPage((p) => Math.min(p + 1, totalPages));
  };

  const prevPage = () => {
    setPage((p) => Math.max(p - 1, 1));
  };

  const goToPage = (p: number) => {
    if (p < 1 || p > totalPages) return;
    setPage(p);
  };

  const resetPage = () => setPage(1);

  return {
    page,
    totalPages,
    paginatedData,
    nextPage,
    prevPage,
    goToPage,
    setPage,
    resetPage,
  };
};