export function formatDateTimeToMinute(value: string | null, includeYear = true) {
  if (!value) {
    return '--'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return trimSeconds(value)
  }

  return new Intl.DateTimeFormat('zh-CN', {
    ...(includeYear ? { year: 'numeric' as const } : {}),
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export function trimSeconds(value: string) {
  return value.replace('T', ' ').replace(/:\d{2}$/, '')
}
