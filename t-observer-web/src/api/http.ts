import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

http.interceptors.request.use((config) => {
  const token = window.localStorage.getItem('t-observer-token')

  if (token) {
    config.headers.set('X-Auth-Token', token)
  }

  return config
})
