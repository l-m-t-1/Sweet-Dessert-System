import request from './request'
export const pageDesserts = params => request.get('/dessert/page', { params })
export const createDessert = data => request.post('/dessert', data)
export const updateDessert = (id, data) => request.put(`/dessert/${id}`, data)
export const deleteDessert = id => request.delete(`/dessert/${id}`)
export const changeDessertStatus = (id, status) => request.patch(`/dessert/${id}/status`, { status })
export const uploadDessertImage = file => {
  const data = new FormData()
  data.append('file', file)
  return request.post('/upload/dessert', data)
}
