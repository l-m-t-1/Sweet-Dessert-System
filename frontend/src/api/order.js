import request from './request'
export const pageOrders = params => request.get('/orders', { params })
export const getOrder = id => request.get(`/orders/${id}`)
export const createOrder = data => request.post('/orders', data)
export const completeOrder = id => request.put(`/orders/${id}/complete`)
export const cancelOrder = id => request.put(`/orders/${id}/cancel`)
