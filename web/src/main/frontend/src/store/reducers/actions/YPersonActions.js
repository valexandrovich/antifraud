export const SET_YPERSON_FORM_VALUE = "SET_YPERSONS_FORM_VALUE";
export const RESET_YPERSON_FORM_VALUES = "RESET_YPERSON_FORM_VALUES";
export const SET_CURRENT_YPERSON_PAGE = "SET_CURRENT_YPERSON_PAGE";
export const SET_TOTAL_YPERSON_ELEMENTS = "SET_TOTAL_YPERSON_ELEMENTS";
export const SET_SEARCH_YPERSON_RESULTS = "SET_SEARCH_YPERSON_RESULTS";
export const SET_PER_PAGE_YPERSON = "SET_PER_PAGE_YPERSON";
export const SET_YPERSON_SUBSCRIPTION = "SET_YPERSON_SUBSCRIPTION";

export const changeYPersonFormValueAC = (input, value) => {
  return {
    type: SET_YPERSON_FORM_VALUE,
    input: input,
    value: value,
  };
};
export const resetFormValueAc = () => {
  return {
    type: RESET_YPERSON_FORM_VALUES,
  };
};

export const setCurrentPageCount = (page) => {
  return {
    type: SET_CURRENT_YPERSON_PAGE,
    page: page,
  };
};

export const setTotalElementsCount = (count) => {
  return {
    type: SET_TOTAL_YPERSON_ELEMENTS,
    count: count,
  };
};

export const setPerPageCount = (count) => {
  return {
    type: SET_PER_PAGE_YPERSON,
    count: count,
  };
};

export const searchResultsAC = (search) => {
  return {
    type: SET_SEARCH_YPERSON_RESULTS,
    search: search,
  };
};

// export const subscriptionYPerson = (id, sub) => {
//   return {
//     type: SET_YPERSON_SUBSCRIPTION,
//     sub: sub,
//     id: id,
//   };
// };
