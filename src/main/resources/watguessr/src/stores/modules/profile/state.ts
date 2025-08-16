export interface ProfileState {
  profileUserId: string | null;
}

export const state = (): ProfileState => ({
  profileUserId: null
});
