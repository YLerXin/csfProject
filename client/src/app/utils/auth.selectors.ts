
import { createFeatureSelector,createSelector } from "@ngrx/store";
import { AuthState } from "./auth.reducer";

export const selectAuthState= createFeatureSelector<AuthState>('auth');

export const selectUser = createSelector(
    selectAuthState,
    (state:AuthState)=>state.user
);

export const selectUserId = createSelector(
    selectUser,
    (user)=>user?.userId
);

export const selectUserName=createSelector(
    selectUser,
    (user)=>user?.username
)
export const selectUserEmail=createSelector(
    selectUser,
    (user)=>user?.email
)
export const selectUserPic=createSelector(
    selectUser,
    (user)=>user?.profilePicUrl
)