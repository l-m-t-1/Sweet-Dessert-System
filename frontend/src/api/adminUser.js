import request from './request'

export const pageUsers = params => request.get('/admin/users', { params })
export const changeUserStatus = (id, status) =>
  request.patch(`/admin/users/${id}/status`, { status })
