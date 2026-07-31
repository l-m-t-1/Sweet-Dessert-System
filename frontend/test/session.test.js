import test from 'node:test'
import assert from 'node:assert/strict'

import { clearSession, readSession, saveSession } from '../src/auth/session.js'
import { canAccess, homeForRole } from '../src/auth/access.js'

function memoryStorage() {
  const values = new Map()
  return {
    getItem: key => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: key => values.delete(key),
  }
}

test('session saves and clears JWT account data', () => {
  const storage = memoryStorage()
  const account = { token: 'jwt', userId: 7, username: 'alice', role: 'USER' }

  saveSession(account, storage)
  assert.deepEqual(readSession(storage), account)

  clearSession(storage)
  assert.equal(readSession(storage), null)
})

test('invalid session data is treated as logged out', () => {
  const storage = memoryStorage()
  storage.setItem('dessert-session', '{broken')
  assert.equal(readSession(storage), null)
})

test('role access separates customer store and admin pages', () => {
  assert.equal(homeForRole('ADMIN'), '/admin/home')
  assert.equal(homeForRole('USER'), '/shop')
  assert.equal(canAccess('ADMIN', 'ADMIN'), true)
  assert.equal(canAccess('USER', 'ADMIN'), false)
  assert.equal(canAccess('USER', 'USER'), true)
})
