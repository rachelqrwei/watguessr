import type { ProfileState } from './state';

export const mutations = {
  SET_PROFILE_USER_ID(state: ProfileState, userId: string | null) {
    state.profileUserId = userId;
  },

  CLEAR_PROFILE_USER_ID(state: ProfileState) {
    state.profileUserId = null;
  }
};
