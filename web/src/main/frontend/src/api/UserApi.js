import axios from "axios";

import authHeader from "./AuthHeader";

const getSubscribed = async (pageNo, pageSize) => {
  try {
    const resp = await axios.post(
      "/api/user/subscriptionsPerson",
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
    return err.data;
  }
};
const getSubscribedCompany = async (pageNo, pageSize) => {
  try {
    const resp = await axios.post(
      "/api/user/subscriptionsCompany",
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
    return err.data;
  }
};

const subscribe = async (id) => {
  try {
    const resp = await axios.put(`/api/user/subscribePerson/${id}`, null, {
      headers: authHeader(),
    });

    return resp.data;
  } catch (err) {
    console.log(err);
  }
};
const subscribeCompany = async (id) => {
  try {
    const resp = await axios.put(`/api/user/subscribeCompany/${id}`, null, {
      headers: authHeader(),
    });
    return resp.data;
  } catch (err) {
    console.log(err);
  }
};

const unsubscribe = async (id) => {
  try {
    const resp = await axios.put(`/api/user/unsubscribePerson/${id}`, null, {
      headers: authHeader(),
    });

    return resp.data;
  } catch (err) {
    console.log(err);
  }
};
const unsubscribeCompany = async (id) => {
  try {
    const resp = await axios.put(`/api/user/unsubscribeCompany/${id}`, null, {
      headers: authHeader(),
    });

    return resp.data;
  } catch (err) {
    console.log(err);
  }
};

const getComparisonsPerson = async () => {
  try {
    const resp = await axios.get("/api/user/comparisonsPerson", {
      headers: authHeader(),
    });
    return resp.data;
  } catch (err) {
    console.log(err);
  }
};
const unComparePerson = async (id) => {
  try {
    const resp = await axios.put(`/api/user/unComparePerson/${id}`, null, {
      headers: authHeader(),
    });
    return resp.data;
  } catch (err) {
    return err.response;
  }
};

const removePersonFromRelation = async (personId, groupId) => {
  try {
    const resp = await axios.post(
      "/api/yperson/removePersonFromRelation",
      null,
      {
        headers: authHeader(),
        params: {
          personId,
          groupId,
        },
      }
    );
    return resp.data;
  } catch (err) {
    return err.response;
  }
};

const userService = {
  getSubscribed,
  subscribe,
  unsubscribe,
  getSubscribedCompany,
  subscribeCompany,
  unsubscribeCompany,
  getComparisonsPerson,
  unComparePerson,
  removePersonFromRelation,
};
export default userService;
