import type { ProfileState } from './state';

export const getters = {
  getProfileUserId: (state: ProfileState) => state.profileUserId,
};
