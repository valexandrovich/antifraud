import axios from "axios";
import { authenticate, logoutUserThunk } from "../store/reducers/AuthReducer";
import jwt_decode from "jwt-decode";

const auth = async (authData) => {
  const resp = await axios.post("/authenticate", authData);
  localStorage.setItem("user", JSON.stringify(resp.data.token.token));
  localStorage.setItem("userName", JSON.stringify(resp.data.userName));
  return resp;
};

const logout = () => {
  localStorage.removeItem("user");
};

const getCurrentUser = () => {
  return JSON.parse(localStorage.getItem("user"));
};

const authService = {
  auth,
  logout,
  getCurrentUser,
};

const runLogoutTimer = (dispatch, timer, logOutTimeout) => {
  logOutTimeout.timeout = setTimeout(() => {
    logOutTimeout.timeout = null;
    dispatch(logoutUserThunk());
  }, timer);
};

export const checkAutoLogin = (dispatch, history, logOutTimeout, location) => {
  if (logOutTimeout.timeout != null) {
    clearTimeout(logOutTimeout.timeout);
    logOutTimeout.timeout = null;
  }
  const tokenDetailsString = localStorage.getItem("user");
  let decoded = tokenDetailsString && jwt_decode(tokenDetailsString);
  if (!tokenDetailsString || decoded.role == null) {
    dispatch(logoutUserThunk());
    return;
  }
  dispatch(authenticate());
  history.replace(location === "/" ? "/search" : location);
  let expireDate = decoded.exp * 1000 - 5000;
  let iatDate = Date.now();
  if (iatDate >= expireDate) {
    dispatch(logoutUserThunk());
    return;
  }
  const timer = expireDate - iatDate;
  runLogoutTimer(dispatch, timer, logOutTimeout);
};

export default authService;
