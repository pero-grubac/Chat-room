import Login from "./Components/LoginSingUp/Login";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Register from "./Components/LoginSingUp/Register";
import { Error404 } from "./Components/Error";
import { useDispatch, useSelector } from "react-redux";
import ForumRoom from "./Components/ForumRooms/ForumRooms";
import { useEffect } from "react";
import { authState } from "./Components/Redux/slices/userSlice";
import Users from "./Components/Users/Users";
import { ROLE_ADMIN } from "./Components/Util/RolePermission";

const App = () => {
  const dispatch = useDispatch();
  const { role, authenticated, initialized } = useSelector((state) => state.users);

  useEffect(() => {
    dispatch(authState());
  }, [dispatch]);

  // Until the session is known, a signed-in user would be redirected away from their page
  if (!initialized) return null;

  const signIn = <Navigate to="/login" replace />;
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={authenticated ? "/forum_rooms" : "/login"} replace />} />
        <Route path="/login" element={authenticated ? <Navigate to="/forum_rooms" replace /> : <Login />} />
        <Route path="/register" element={<Register />} />
        {/* Forum and moderator parts: every signed-in group */}
        <Route path="/forum_rooms" element={authenticated ? <ForumRoom /> : signIn} />
        {/* Administrator part */}
        <Route path="/users" element={!authenticated ? signIn : role === ROLE_ADMIN ? <Users /> : <Error404 />} />
        <Route path="*" element={authenticated ? <Error404 /> : signIn} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
