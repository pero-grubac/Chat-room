import { useCallback } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { logout } from "../Redux/slices/userSlice";
import { isSessionLost } from "../Error/ErrorMessage";

/**
 * Returns a handler for failed requests: if the server ended the session (401, or a WAF block,
 * which closes it), the user is signed out locally and sent to the login page. Other errors
 * (a duplicate name, a missing record…) leave the user where they are.
 */
export const useSessionGuard = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  return useCallback(
    async (error) => {
      if (isSessionLost(error)) {
        await dispatch(logout());
        navigate("/login", { replace: true });
      }
    },
    [dispatch, navigate]
  );
};
