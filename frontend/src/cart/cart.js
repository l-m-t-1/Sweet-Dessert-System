const CART_KEY = 'dessert-cart'

const normalizedItem = product => ({
  id: product.id,
  name: product.name,
  price: Number(product.price),
  stock: Number(product.stock),
  image: product.image || '',
  quantity: 1,
})

export function addCartItem(cart, product) {
  const found = cart.find(item => item.id === product.id)
  if (!found) return [...cart, normalizedItem(product)]
  return cart.map(item => item.id === product.id
    ? { ...item, stock: Number(product.stock), quantity: Math.min(item.quantity + 1, Number(product.stock)) }
    : item)
}

export function setCartQuantity(cart, id, quantity) {
  if (quantity <= 0) return cart.filter(item => item.id !== id)
  return cart.map(item => item.id === id
    ? { ...item, quantity: Math.min(Number(quantity), item.stock) }
    : item)
}

export const cartTotal = cart =>
  cart.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)

export function readCart(storage = typeof localStorage === 'undefined' ? null : localStorage) {
  if (!storage) return []
  try {
    const value = JSON.parse(storage.getItem(CART_KEY))
    return Array.isArray(value) ? value : []
  } catch {
    return []
  }
}

export function saveCart(cart, storage = typeof localStorage === 'undefined' ? null : localStorage) {
  storage?.setItem(CART_KEY, JSON.stringify(cart))
}

export function clearCart(storage = typeof localStorage === 'undefined' ? null : localStorage) {
  storage?.removeItem(CART_KEY)
}
