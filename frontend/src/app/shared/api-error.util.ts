export function extractApiErrorMessage(
  error: any,
  fallback = 'Une erreur est survenue.'
): string {
  const backendMessage =
    error?.error?.message ??
    error?.error?.detail ??
    error?.error?.error ??
    error?.message;

  if (typeof backendMessage === 'string' && backendMessage.trim().length > 0) {
    return cleanupBackendMessage(backendMessage);
  }

  if (Array.isArray(error?.error?.errors) && error.error.errors.length > 0) {
    return error.error.errors
      .map((item: any) => String(item))
      .join(', ');
  }

  return fallback;
}

export function getHttpErrorUserMessage(error: any): string {
  if (!error) {
    return 'Une erreur est survenue.';
  }

  if (error.status === 0) {
    return 'Backend indisponible. Vérifie que Spring Boot est bien lancé sur le port 8080.';
  }

  if (error.status === 400) {
    return extractApiErrorMessage(error, 'La demande envoyée est invalide.');
  }

  if (error.status === 401) {
    return extractApiErrorMessage(error, 'Identification requise.');
  }

  if (error.status === 403) {
    return extractApiErrorMessage(error, 'Accès refusé pour cet utilisateur.');
  }

  if (error.status === 404) {
    return extractApiErrorMessage(error, 'Ressource introuvable.');
  }

  if (error.status === 409) {
    return extractApiErrorMessage(error, 'Action impossible : conflit avec les données existantes.');
  }

  if (error.status >= 500) {
    return extractApiErrorMessage(
      error,
      'Erreur technique côté serveur. Consulte la console backend si le problème persiste.'
    );
  }

  return extractApiErrorMessage(error);
}

function cleanupBackendMessage(message: string): string {
  return message
    .replace(/^(\d{3}\s)?[A-Z_]+ "?/g, '')
    .replace(/"$/, '')
    .trim();
}
