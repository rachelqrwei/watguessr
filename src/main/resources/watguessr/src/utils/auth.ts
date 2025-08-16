// Authentication utility functions

/**
 * Get the stored JWT token from localStorage
 */
export const getStoredToken = (): string | null => {
  return localStorage.getItem('jwt_token');
};

/**
 * Get the stored user data from localStorage
 */
export const getStoredUser = (): any => {
  const userData = localStorage.getItem('user_data');
  if (userData) {
    try {
      return JSON.parse(userData);
    } catch (e) {
      return null;
    }
  }
  return null;
};

/**
 * Remove the stored JWT token from localStorage
 */
export const removeStoredToken = (): void => {
  localStorage.removeItem('jwt_token');
};

/**
 * Check if a token exists and is not expired
 * Note: This is a basic check - the server will validate the actual token
 */
export const hasValidToken = (): boolean => {
  const token = getStoredToken();
  return token !== null && token.length > 0;
};

/**
 * Create Authorization header with Bearer token
 */
export const createAuthHeader = (): { Authorization: string } | {} => {
  const token = getStoredToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

/**
 * Create headers object with authentication and content type
 */
export const createHeaders = (): Record<string, string> => {
  const token = getStoredToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  };
  
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  
  return headers;
}; 