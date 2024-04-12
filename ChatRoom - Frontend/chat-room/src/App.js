
import "./App.css";
import Login from "./Components/LoginSingUp/Login";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Register from "./Components/LoginSingUp/Register";
import { Error403, Error404, Error406, Error500, UnknownError } from "./Components/Error";
import ForumRoom from "./Components/ForumRooms/ForumRooms";

const App = () => {
  return (
    <BrowserRouter>
      <div>
        <Routes>
          <Route exact path="/login" element={<Login />} />
          <Route exact path="/forum_rooms" element={<ForumRoom />} />
          <Route exact path="/" element={<Navigate to="/login" />} />
          <Route exact path="/register" element={<Register />} />
          <Route exact path="/error403" element={<Error403 />} />
          <Route exact path="/error404" element={<Error404 />} />
          <Route exact path="/error406" element={<Error406 />} />
          <Route exact path="/error500" element={<Error500 />} />
          <Route exact path="/unknown" element={<UnknownError />} />
          <Route path="*" element={<Navigate to="/error404" />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
};

export default App;
