export interface BugReport {
  title: string
  category: string
  description: string
  includeUserInfo: boolean
}

export interface BugReportState {
  loading: boolean
  error: string | null
  success: string | null
}

export const state = (): BugReportState => ({
  loading: false,
  error: null,
  success: null
})
