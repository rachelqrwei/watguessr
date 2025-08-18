import type { BugReportState, BugReport } from './state'

export const mutations = {
  SET_LOADING(state: BugReportState, loading: boolean) {
    state.loading = loading
  },

  SET_ERROR(state: BugReportState, error: string | null) {
    state.error = error
  },

  SET_SUCCESS(state: BugReportState, success: string | null) {
    state.success = success
  },

  RESET_STATE(state: BugReportState) {
    state.error = null
    state.success = null
  }
}
