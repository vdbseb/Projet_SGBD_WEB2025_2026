export function extractApiErrorMessage(
  error: any,
  fallback = 'Une erreur est survenue.'
): string {
  return (
    error?.error?.message ??
    error?.error?.detail ??
    error?.error?.error ??
    error?.message ??
    fallback
  );
}
