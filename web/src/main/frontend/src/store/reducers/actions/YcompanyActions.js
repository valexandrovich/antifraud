export const SET_YCOMPANY_FORM_VALUE = "SET_YCOMPANY_FORM_VALUE";
export const SET_SEARCH_YCOMPANY_RESULTS = "SET_SEARCH_YCOMPANY_RESULTS";
export const SET_TOTAL_YCOMPANY_ELEMENTS = "SET_TOTAL_YCOMPANY_ELEMENTS";
export const RESET_YCOMPANY_FORM_VALUES = "RESET_YCOMPANY_FORM_VALUES";
export const SET_PER_PAGE_YCOMPANY = "SET_PER_PAGE_YCOMPANY";
export const SET_CURRENT_PAGE_YCOMPANY = "SET_CURRENT_PAGE_YCOMPANY";
export const changeYCompanyFormValueAC = (input, value) => {
  return {
    type: SET_YCOMPANY_FORM_VALUE,
    input: input,
    value: value,
  };
};
export const searchResultsYCompanyAC = (search) => {
  return {
    type: SET_SEARCH_YCOMPANY_RESULTS,
    search: search,
  };
};

export const setTotalElementsYCompanyCount = (count) => {
  return {
    type: SET_TOTAL_YCOMPANY_ELEMENTS,
    count: count,
  };
};

export const resetYCompanyFormValueAc = () => {
  return {
    type: RESET_YCOMPANY_FORM_VALUES,
  };
};

export const setPerPageYCompanyCount = (count) => {
  return {
    type: SET_PER_PAGE_YCOMPANY,
    count: count,
  };
};

export const setCurrentPageYcompanyCount = (page) => {
  return {
    type: SET_CURRENT_PAGE_YCOMPANY,
    page: page,
  };
};

// export const searchResultsAC = (search) => {
//   return {
//     type: SET_SEARCH_RESULTS,
//     search: search,
//   };
// };
//
// export const setPerPageCount = (count) => {
//   return {
//     type: SET_PER_PAGE,
//     count: count,
//   };
// };
