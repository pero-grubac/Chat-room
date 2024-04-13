import "./App.css";
import Login from "./Components/LoginSingUp/Login";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Register from "./Components/LoginSingUp/Register";
import { Error404 } from "./Components/Error";
import { useDispatch, useSelector } from "react-redux";
import ForumRoom from "./Components/ForumRooms/ForumRooms";
import React, { useEffect } from "react";
import { authState } from "./Components/Redux/slices/userSlice";

const App = () => {
  const {
    user,
    role,
    permissions,
    authenticated,
    authenticationFailed,
    loading,
  } = useSelector((state) => state.users);

  const dispatch = useDispatch();
  useEffect(() => {
    dispatch(authState());
  }, []);

  return (
    <BrowserRouter>
      <div>
        <Routes>
          <Route
            exact
            path="/login"
            element={
              !authenticated ? <Login /> : <Navigate to="/forum_rooms" />
            }
          />
          <Route
            exact
            path="/forum_rooms"
            element={authenticated ? <ForumRoom /> : <Navigate to="/login" />}
            />
          <Route exact path="/" element={<Navigate to="/login" />} />
          <Route exact path="/register" element={<Register />} />
          <Route exact path="/error404" element={<Error404 />} />
          <Route path="*" element={<Navigate to="/error404" />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
};

export default App;
