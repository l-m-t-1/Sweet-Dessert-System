import request from './request'

export const pageStoreDesserts = params => request.get('/store/desserts', { params })
