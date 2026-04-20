import { defineStore } from 'pinia'

type RoleCode = 'LEADER' | 'MEMBER' | 'ADMIN' | ''

type LoginPayload = {
  token: string
  roleCode: Exclude<RoleCode, ''>
  realName: string
  userId: number
}

type AuthState = {
  token: string
  roleCode: RoleCode
  realName: string
  userId: number
}

const STORAGE_KEYS = {
  token: 't-observer-token',
  roleCode: 't-observer-role',
  realName: 't-observer-name',
  userId: 't-observer-user-id',
} as const

function readAuthState(): AuthState {
  return {
    token: window.localStorage.getItem(STORAGE_KEYS.token) ?? '',
    roleCode: (window.localStorage.getItem(STORAGE_KEYS.roleCode) as RoleCode | null) ?? '',
    realName: window.localStorage.getItem(STORAGE_KEYS.realName) ?? '',
    userId: Number(window.localStorage.getItem(STORAGE_KEYS.userId) ?? 0),
  }
}

function persistAuthState(state: AuthState) {
  window.localStorage.setItem(STORAGE_KEYS.token, state.token)
  window.localStorage.setItem(STORAGE_KEYS.roleCode, state.roleCode)
  window.localStorage.setItem(STORAGE_KEYS.realName, state.realName)
  window.localStorage.setItem(STORAGE_KEYS.userId, String(state.userId))
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => readAuthState(),
  actions: {
    acceptLogin(payload: LoginPayload) {
      this.token = payload.token
      this.roleCode = payload.roleCode
      this.realName = payload.realName
      this.userId = payload.userId

      persistAuthState(this.$state)
    },
  },
})
