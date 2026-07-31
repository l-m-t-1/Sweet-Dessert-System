const SESSION_KEY = 'dessert-session'

const defaultStorage = () => typeof sessionStorage === 'undefined' ? null : sessionStorage

export function readSession(storage = defaultStorage()) {
  if (!storage) return null
  try {
    const session = JSON.parse(storage.getItem(SESSION_KEY))
    return session?.token && session?.role ? session : null
  } catch {
    return null
  }
}

export function saveSession(session, storage = defaultStorage()) {
  storage?.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession(storage = defaultStorage()) {
  storage?.removeItem(SESSION_KEY)
}
