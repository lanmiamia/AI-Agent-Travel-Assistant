export function createChatId(prefix = 'chat') {
  const randomPart =
    typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`

  return `${prefix}_${randomPart}`
}

export function getOrCreateChatId(storageKey, prefix) {
  try {
    const storedChatId = sessionStorage.getItem(storageKey)

    if (storedChatId) {
      return storedChatId
    }

    const chatId = createChatId(prefix)
    sessionStorage.setItem(storageKey, chatId)
    return chatId
  } catch {
    return createChatId(prefix)
  }
}