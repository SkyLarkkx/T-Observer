export type RoleCode = 'LEADER' | 'MEMBER' | 'ADMIN'

export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  token: string
  userId: number
  realName: string
  roleCode: RoleCode
}

export type CurrentUserResponse = {
  userId: number
  username: string
  realName: string
  roleCode: RoleCode
}

export type MemberOption = CurrentUserResponse

export type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}
