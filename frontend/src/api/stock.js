import request from './request'
export const pageStockRecords = params => request.get('/stock-records', { params })
export const adjustStock = data => request.post('/stock-records/adjust', data)
