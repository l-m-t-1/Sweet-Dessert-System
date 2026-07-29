import test from 'node:test'
import assert from 'node:assert/strict'
import config from '../vite.config.js'

test('api proxy preserves the api prefix', () => {
  const proxy = config.server.proxy['/api']

  assert.equal(proxy.target, 'http://localhost:8080')
  assert.equal(Object.hasOwn(proxy, 'rewrite'), false)
})

test('uploads keep their original path', () => {
  const proxy = config.server.proxy['/uploads']

  assert.equal(proxy.target, 'http://localhost:8080')
  assert.equal(Object.hasOwn(proxy, 'rewrite'), false)
})
