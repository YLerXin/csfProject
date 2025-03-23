import { createAction,props } from "@ngrx/store";
import { User } from "../model/AuthRequest";


export const loginSuccess = createAction(
    '[Auth] Login Success',
    props<{user:User}>(),


);

export const logoutSuccess = createAction('[Auth] Logout Success');
