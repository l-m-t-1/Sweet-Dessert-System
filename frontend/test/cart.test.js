import test from 'node:test'
import assert from 'node:assert/strict'

import { addCartItem, cartTotal, setCartQuantity } from '../src/cart/cart.js'

test('cart merges products and respects available stock', () => {
  let cart = addCartItem([], { id: 1, name: '巧克力蛋糕', price: 28, stock: 2 })
  cart = addCartItem(cart, { id: 1, name: '巧克力蛋糕', price: 28, stock: 2 })
  cart = addCartItem(cart, { id: 1, name: '巧克力蛋糕', price: 28, stock: 2 })

  assert.equal(cart.length, 1)
  assert.equal(cart[0].quantity, 2)
  assert.equal(cartTotal(cart), 56)
})

test('setting quantity to zero removes a product', () => {
  const cart = [{ id: 1, name: '布丁', price: 12, stock: 3, quantity: 1 }]
  assert.deepEqual(setCartQuantity(cart, 1, 0), [])
})
