import type { Module } from 'vuex'
import type { RootState } from '@/stores/index'
import { state, type BugReportState } from './state'
import { mutations } from './mutations'
import { actions } from './actions'
import { getters } from './getters'

const bugReportModule: Module<BugReportState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

export default bugReportModule
