import type { BugReportState } from './state'

export const getters = {
  getLoading: (state: BugReportState) => state.loading,

  getError: (state: BugReportState) => state.error,

  getSuccess: (state: BugReportState) => state.success,

  isSubmitting: (state: BugReportState) => state.loading
}
