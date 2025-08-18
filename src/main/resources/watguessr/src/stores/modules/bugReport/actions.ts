import type { BugReportState, BugReport } from './state'

export const actions = {
  async submitBugReport({ state, commit, rootState }: { state: BugReportState; commit: any; rootState: any }, bugReport: BugReport) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    commit('SET_SUCCESS', null)

    try {
      // Get current user info if they want to include it
      const currentUser = rootState.user.currentUser
      const userInfo = bugReport.includeUserInfo && currentUser
        ? `\n\nUser: ${currentUser.username}`
        : ''

      // Prepare email content
      const emailContent = `
Bug Report: ${bugReport.title}

Category: ${bugReport.category}

Description:
${bugReport.description}

Steps to Reproduce:
${bugReport.steps}

Browser: ${bugReport.browser || 'Not specified'}
Device: ${bugReport.device || 'Not specified'}
${userInfo}

---
Reported via WatGuessr Bug Report Form
      `.trim()

      const response = await fetch('/api/user/report-bug', {
        method: 'POST',
        credentials: "include",
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          subject: `Bug Report: ${bugReport.title}`,
          content: emailContent,
          category: bugReport.category
        })
      })

      if (!response.ok) {
        throw new Error('Failed to send bug report')
      }

      commit('SET_SUCCESS', 'Thank you! Your bug report has been submitted successfully.')
      return { success: true }

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to submit bug report'
      commit('SET_ERROR', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      commit('SET_LOADING', false)
    }
  },

  resetState({ commit }: { commit: any }) {
    commit('RESET_STATE')
  }
}
