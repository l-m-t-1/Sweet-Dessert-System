import request from './request'

export const pageMyOrders = params => request.get('/customer/orders', { params })
export const getMyOrder = id => request.get(`/customer/orders/${id}`)
export const createMyOrder = data => request.post('/customer/orders', data)
export const cancelMyOrder = id => request.put(`/customer/orders/${id}/cancel`)
