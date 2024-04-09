import "./App.css";
import Login from "./Components/LoginSingUp/Login";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Register from "./Components/LoginSingUp/Register";
import { Error403, Error404, Error406, Error500 } from "./Components/Error";
const App = () => {
  return (
    <BrowserRouter>
      <div>
        <Routes>
          <Route exact path="/login" element={<Login />} />
          <Route exact path="/register" element={<Register />} />
          <Route exact path="/error403" element={<Error403 />} />
          <Route exact path="/error404" element={<Error404 />} />
          <Route exact path="/error406" element={<Error406 />} />
          <Route exact path="/error500" element={<Error500 />} />
          <Route path="*" element={<Navigate to="/error404" />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
};

export default App;
