import axios from "axios";
import { setAlertMessageThunk } from "../store/reducers/AuthReducer";
import authHeader from "./AuthHeader";

const getSubscribed = async (pageNo, pageSize, dispatch) => {
  try {
    const resp = await axios.post(
      "/api/user/subscriptions",
      JSON.stringify({
        direction: "ASC",
        page: pageNo,
        properties: ["id"],
        size: pageSize,
      }),
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization:
            "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
        },
      }
    );
    return resp.data;
  } catch (err) {
    dispatch(
      setAlertMessageThunk(
        err.response.data.messages.map((el) => el),
        "danger"
      )
    );
  }
};

const subscribe = async (id) => {
  try {
    const resp = await axios.put(`/api/user/subscribe/${id}`, null, {
      headers: authHeader(),
    });
    return resp.data;
  } catch (err) {
    console.log(err);
  }
};

const unsubscribe = async (id) => {
  try {
    const resp = await axios.put(`/api/user/unsubscribe/${id}`, null, {
      headers: authHeader(),
    });
    return resp.data;
  } catch (err) {
    console.log(err);
  }
};

const userService = {
  getSubscribed,
  subscribe,
  unsubscribe,
};
export default userService;
