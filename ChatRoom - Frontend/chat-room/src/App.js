import "./App.css";
import Login from "./Components/LoginSingUp/Login";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Register from "./Components/LoginSingUp/Register";
import { Error404 } from "./Components/Error";
//import { useDispatch, useSelector } from "react-redux";
import ForumRoom from "./Components/ForumRooms/ForumRooms";
import React, { useEffect } from "react";
import { authState } from "./Components/Redux/slices/userSlice";
import Users from "./Components/Users/Users";

const App = () => {
//  const dispatch = useDispatch();
  /*const {
    user,
    role,
    permissions,
    authenticated,
    authenticationFailed,
    loading,
  } = useSelector((state) => state.users);
  useEffect(() => {
    dispatch(authState());
  }, [dispatch]);*/

  return (
    <BrowserRouter>
      <div>
        <Routes>
          <Route exact path="/login" element={<Login />} />

          <Route exact path="/forum_rooms" element={<ForumRoom />} />
          <Route exact path="/users" element={<Users />} />
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
