import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import userService from "../../../Services/user.service";
import loginService from "../../../Services/login.service";
import { jwtDecode } from "jwt-decode";

const logoutAction = (state, action) => {
  loginService.logout();
  return ;
};

const login = createAsyncThunk("auth/login", ({ username, password }) =>
  loginService.loginUser(username, password)
);
export const logintoken = createAsyncThunk("auth/token", ({ username, password, email }) =>
  loginService.token(username, password, email)
);
const getAll = createAsyncThunk("users/getUsers", () => userService.getUsers());


const approveUser = createAsyncThunk("users/approve", ({ user }) =>
  userService.approveUser(user)
);

const deleteUser = createAsyncThunk("users/delete", ({ id }) =>
  userService.deleteUser(id)
);

const requests = createAsyncThunk("users/requests", () =>
  userService.requests()
);

const getApprovedUser = createAsyncThunk("users/approved", ({ id }) =>
  userService.getApprovedUser(id)
);

const updateUser = createAsyncThunk("users/update", ({ user }) =>
  userService.updateUser(user)
);

const updateElements = (arr, obj) => {
  return arr.map((el) => (el.id === obj.id ? { ...el, ...obj } : el));
};

const onSuccessAuth = (state, action) => {
  const token = action.payload;
  if (typeof token !== 'string') {
    return;
  }
    const user = jwtDecode(token);
    state.authenticated = true;
    state.authenticationFailed = false;
    state.loading = false;
    state.role = user.role;
    state.user = user.sub;
    state.permissions = user.permissions;
  
};

export const authState = createAsyncThunk("users/jwt", () => {
  const item = sessionStorage.getItem("auth");
  if (item !== null || item !== undefined || item !== "") {
    return item;
  } else {
    return "";
  }
});

const userSlice = createSlice({
  name: "users",
  initialState: {
    authenticated: false,
    authenticationFailed: false,
    loading: true,
    user: null,
    role: null,
    permissions: [],
    users: [],
  },
  reducers: {
    logout: logoutAction,
  },
  extraReducers: (builder) => {
    builder
      .addCase(getAll.fulfilled, (state, action) => {
        state.users = action.payload;
      })
      .addCase(login.pending, (state, action) => {
        state.loading = true;
      })
      .addCase(login.rejected, (state, action) => {
        state.authenticationFailed = true;
        state.loading = true;
      })
      .addCase(logintoken.pending, (state, action) => {
        state.loading = true;
      })
      .addCase(logintoken.rejected, (state, action) => {
        state.authenticationFailed = true;
        state.loading = true;
      })
      .addCase(logintoken.fulfilled, onSuccessAuth)
      .addCase(authState.pending, (state, action) => {
        state.loading = true;
      })
      .addCase(authState.rejected, (state, action) => {
        state.loading = false;
      })
      .addCase(authState.fulfilled, onSuccessAuth)
      
      .addCase(approveUser.fulfilled, (state, action) => {
        state.users = updateElements(state.users, action.payload);
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.users = updateElements(state.users, action.payload);
      })
      .addCase(requests.fulfilled, (state, action) => {
        state.users = action.payload;
      })
      .addCase(getApprovedUser.fulfilled, (state, action) => {
        state.users = action.payload;
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        state.user = { ...state.user, ...action.payload };
        state.users = updateElements(state.users, action.payload);
      });
  },
});
export const { logout } = userSlice.actions;
export default userSlice.reducer;