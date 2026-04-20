import { defineStore } from 'pinia'

import type { CurrentUserResponse, LoginResponse, RoleCode } from '@/types/auth'

type AuthState = {
  token: string
  roleCode: RoleCode | ''
  realName: string
  userId: number
  isValidated: boolean
}

const STORAGE_KEYS = {
  token: 't-observer-token',
  roleCode: 't-observer-role',
  realName: 't-observer-name',
  userId: 't-observer-user-id',
} as const

const ROLE_LABELS: Record<RoleCode, string> = {
  LEADER: '组长',
  MEMBER: '教师',
  ADMIN: '管理员',
}

function readAuthState(): AuthState {
  return {
    token: window.localStorage.getItem(STORAGE_KEYS.token) ?? '',
    roleCode: (window.localStorage.getItem(STORAGE_KEYS.roleCode) as RoleCode | null) ?? '',
    realName: window.localStorage.getItem(STORAGE_KEYS.realName) ?? '',
    userId: Number(window.localStorage.getItem(STORAGE_KEYS.userId) ?? 0),
    isValidated: false,
  }
}

function persistAuthState(state: Pick<AuthState, 'token' | 'roleCode' | 'realName' | 'userId'>) {
  if (state.token) {
    window.localStorage.setItem(STORAGE_KEYS.token, state.token)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.token)
  }

  if (state.roleCode) {
    window.localStorage.setItem(STORAGE_KEYS.roleCode, state.roleCode)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.roleCode)
  }

  if (state.realName) {
    window.localStorage.setItem(STORAGE_KEYS.realName, state.realName)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.realName)
  }

  if (state.userId) {
    window.localStorage.setItem(STORAGE_KEYS.userId, String(state.userId))
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.userId)
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => readAuthState(),
  getters: {
    roleLabel(state) {
      return state.roleCode ? ROLE_LABELS[state.roleCode] : ''
    },
    isAuthenticated(state) {
      return Boolean(state.token)
    },
  },
  actions: {
    acceptLogin(payload: LoginResponse) {
      this.token = payload.token
      this.roleCode = payload.roleCode
      this.realName = payload.realName
      this.userId = payload.userId
      this.isValidated = true
      persistAuthState(this.$state)
    },
    acceptCurrentUser(payload: CurrentUserResponse) {
      this.roleCode = payload.roleCode
      this.realName = payload.realName
      this.userId = payload.userId
      this.isValidated = true
      persistAuthState(this.$state)
    },
    logout() {
      this.token = ''
      this.roleCode = ''
      this.realName = ''
      this.userId = 0
      this.isValidated = false
      persistAuthState(this.$state)
    },
  },
})
