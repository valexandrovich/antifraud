import axios from "axios";

const search = async (formValues, pageNo, pageSize) => {
  try {
    return await axios.post(
      "/api/ycompany/search",
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

const YCompanyService = {
  search,
};

export default YCompanyService;
