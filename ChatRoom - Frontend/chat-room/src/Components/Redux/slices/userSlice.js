import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import userService from "../../../Services/user.service"
const userSlice = createSlice({
  name: "users",
  initialState: {
    authenticated: false,
    authenticationFailed: false,
    loading: true,
    user: null,
    role: null,
    permissions:[],
    users: [],
  },
});
export const { logout } = userSlice.actions;
export default userSlice.reducer;
