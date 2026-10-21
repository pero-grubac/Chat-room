import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { currentUser, logout as endSession } from "../../../Services/login.service";

const signedOut = {
  authenticated: false,
  user: null,
  role: null,
  permissions: [],
  rooms: [],
};

// The session cookie is httpOnly, so the app asks the server who is signed in
export const authState = createAsyncThunk("users/me", async () => {
  try {
    return (await currentUser()).data;
  } catch {
    return null;
  }
});

// Tells the server to end the session (and drop the cookie); the local state is reset either way
export const logout = createAsyncThunk("auth/logout", async () => {
  try {
    await endSession();
  } catch {
    // Already signed out on the server
  }
});

const userSlice = createSlice({
  name: "users",
  initialState: {
    ...signedOut,
    // true once the stored session has been read, so routes are not decided too early
    initialized: false,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // The payload is the signed-in user as the API returns it: { username, role, permissions, rooms }
      .addCase(authState.fulfilled, (state, action) => {
        const user = action.payload;
        state.initialized = true;
        if (!user?.username) {
          Object.assign(state, signedOut);
          return;
        }
        state.authenticated = true;
        state.user = user.username;
        state.role = user.role;
        state.permissions = user.permissions ?? [];
        state.rooms = user.rooms ?? [];
      })
      .addCase(logout.fulfilled, (state) => {
        Object.assign(state, signedOut);
      });
  },
});

export default userSlice.reducer;
