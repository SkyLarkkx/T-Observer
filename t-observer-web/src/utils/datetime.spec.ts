import { describe, expect, it } from 'vitest'

import { formatDateTimeToMinute, trimSeconds } from './datetime'

describe('datetime utils', () => {
  it('formats datetime values to minute precision', () => {
    expect(formatDateTimeToMinute('2026-04-20T09:30:45')).toBe('2026/04/20 09:30')
    expect(formatDateTimeToMinute('2026-04-20T09:30:45', false)).toBe('04/20 09:30')
  })

  it('trims trailing seconds from raw datetime strings', () => {
    expect(trimSeconds('2026-04-20T09:30:45')).toBe('2026-04-20 09:30')
  })
})
