import type {
  ApiEnvelope,
  CurrentUserResponse,
  LoginRequest,
  LoginResponse,
} from '@/types/auth'

import { http } from './http'

export async function login(payload: LoginRequest) {
  const response = await http.post<ApiEnvelope<LoginResponse>>('/auth/login', payload)
  return response.data.data
}

export async function fetchCurrentUser() {
  const response = await http.get<ApiEnvelope<CurrentUserResponse>>('/auth/me')
  return response.data.data
}
