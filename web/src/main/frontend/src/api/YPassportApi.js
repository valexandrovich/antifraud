import axios from "axios";
import authHeader from "./AuthHeader";

const updatePassport = async (passport) => {
  try {
    const resp = await axios.put("/api/ypassport/update", passport, {
      headers: authHeader(),
    });
    if (resp.status === 200) {
      return resp.data;
    }
  } catch (err) {
    return err.response;
  }
};

const deletePassport = async (id) => {
  try {
    const resp = await axios.delete(`/api/ypassport/delete/${id}`, {
      headers: authHeader(),
    });
    if (resp.status === 200) {
      return resp.data;
    }
  } catch (err) {
    return err.response;
  }
};

const passportService = {
  updatePassport,
  deletePassport,
};

export default passportService;
