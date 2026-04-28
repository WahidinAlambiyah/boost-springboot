"use client";

import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactNode, useState } from "react";

import ApiErrorToast from "@/app/components/api-error-toast";
import { handleGlobalHttpError } from "@/lib/http-error-events";

interface QueryProviderProps {
  children: ReactNode;
}

export default function QueryProvider({ children }: QueryProviderProps) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        queryCache: new QueryCache({
          onError: (error, query) => {
            handleGlobalHttpError(error, {
              retry: async () => {
                await query.fetch();
              },
            });
          },
        }),
        mutationCache: new MutationCache({
          onError: (error, _variables, _context, mutation) => {
            handleGlobalHttpError(error, {
              retry: async () => {
                await mutation.execute(mutation.state.variables);
              },
            });
          },
        }),
        defaultOptions: {
          queries: {
            retry: false,
            refetchOnWindowFocus: false,
          },
          mutations: {
            retry: false,
          },
        },
      }),
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ApiErrorToast />
    </QueryClientProvider>
  );
}
