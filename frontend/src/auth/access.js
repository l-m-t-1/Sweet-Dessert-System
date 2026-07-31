export const homeForRole = role => role === 'ADMIN' ? '/admin/home' : '/shop'

export const canAccess = (actualRole, requiredRole) =>
  !requiredRole || actualRole === requiredRole
