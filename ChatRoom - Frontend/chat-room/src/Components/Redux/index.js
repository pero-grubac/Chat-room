import { configureStore } from "@reduxjs/toolkit";
import userSlice from "./slices/userSlice";
import forumRoomSlice from "./slices/forumRoomSlice";
import commentSlice from "./slices/commentSlice";

export default configureStore({
  reducer: {
    users: userSlice,
    forumRooms: forumRoomSlice,
    comments: commentSlice,
  },
});
