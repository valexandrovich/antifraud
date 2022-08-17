import axios from "axios";
import authHeader from "./AuthHeader";

const search = async (formValues, pageNo, pageSize) => {
  try {
    return await axios.post(
      "/api/yperson/search",
      JSON.stringify({
        paginationRequest: {
          direction: "asc",
          page: pageNo,
          properties: ["id"],
          size: pageSize,
        },
        searchRequest: {
          ...formValues,
        },
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
  } catch (err) {
    return err;
  }
};

const relatedGroupPersons = async (id) => {
  try {
    return await axios.get(`/api/yperson/findByGroupId`, {
      params: { groupId: id },
      headers: authHeader(),
    });
  } catch (err) {
    return err;
  }
};

const joinToExistingRelation = async (groupId, personIds) => {
  try {
    const resp = await axios.post(
      "  /api/yperson/joinToExistingRelation",
      {
        groupId,
        personIds,
      },
      {
        headers: authHeader(),
      }
    );
    return resp.data;
  } catch (err) {
    return err.response;
  }
};
const createNewRelation = async (personIds, typeId) => {
  try {
    const resp = await axios.post(
      "/api/yperson/joinToNewRelation",
      {
        personIds,
        typeId,
      },
      {
        headers: authHeader(),
      }
    );
    return resp.data;
  } catch (err) {
    return err.response;
  }
};

const YPersonService = {
  search,
  relatedGroupPersons,
  joinToExistingRelation,
  createNewRelation,
};
export default YPersonService;
