import axios from "axios";

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

export default authService;

