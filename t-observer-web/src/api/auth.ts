import type {
  ApiEnvelope,
  CurrentUserResponse,
  LoginRequest,
  LoginResponse,
  MemberOption,
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

export async function fetchMembers(keyword?: string) {
  const response = await http.get<ApiEnvelope<MemberOption[]>>('/auth/members', {
    params: keyword ? { keyword } : undefined,
  })
  return response.data.data
}
