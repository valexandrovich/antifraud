import YPersonService from "../../api/YPersonApi";
import {
  setAlertMessageThunk,
  TOGGLE_LOADER,
  toggleLoader,
} from "./actions/Actions";

import {
  RESET_YPERSON_FORM_VALUES,
  searchResultsAC,
  SET_CURRENT_YPERSON_PAGE,
  SET_PER_PAGE_YPERSON,
  SET_SEARCH_YPERSON_RESULTS,
  SET_TOTAL_YPERSON_ELEMENTS,
  SET_YPERSON_FORM_VALUE,
  setCurrentPageCount,
  setTotalElementsCount,
} from "./actions/YPersonActions";

let initialState = {
  fizForm: {
    name: "",
    surname: "",
    patronymic: "",
    day: "",
    month: "",
    year: "",
    age: "",
    phone: "",
    address: "",
    passportNumber: "",
    passportSeria: "",
    id_documentNumber: "",
    id_registryNumber: "",
    foreignP_documentNumber: "",
    foreignP_registryNumber: "",
    inn: "",
  },
  searchResults: [],
  loader: false,
  currentPage: 0,
  perPage: 6,
  totalElements: null,
};

const fisSearchReducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case SET_YPERSON_FORM_VALUE:
      let fizForm = { ...state.fizForm };
      fizForm[action.input] = action.value;
      return {
        ...state,
        fizForm: fizForm,
      };
    case RESET_YPERSON_FORM_VALUES: {
      return {
        ...state,
        fizForm: {
          name: "",
          surname: "",
          patronymic: "",
          day: "",
          month: "",
          year: "",
          age: "",
          phone: "",
          address: "",
          passportNumber: "",
          passportSeria: "",
          id_documentNumber: "",
          id_registryNumber: "",
          foreignP_documentNumber: "",
          foreignP_registryNumber: "",
          inn: "",
        },
        searchResults: [],
        currentPage: initialState.currentPage,
      };
    }
    case SET_SEARCH_YPERSON_RESULTS: {
      return {
        ...state,
        searchResults: action.search,
      };
    }
    case SET_PER_PAGE_YPERSON: {
      return {
        ...state,
        perPage: action.count,
      };
    }
    case SET_CURRENT_YPERSON_PAGE: {
      return {
        ...state,
        currentPage: action.page,
      };
    }
    case SET_TOTAL_YPERSON_ELEMENTS: {
      return {
        ...state,
        totalElements: action.count,
      };
    }
    case TOGGLE_LOADER: {
      return {
        ...state,
        loader: action.status,
      };
    }

    default:
      return state;
  }
};

export const setSearchDataYPersonsThunk =
  (fizForm, pageNo, pageSize) => (dispatch) => {
    dispatch(toggleLoader(true));
    YPersonService.search(fizForm, pageNo, pageSize).then((res) => {
      if (res.response?.status === 500) {
        dispatch(
          setAlertMessageThunk(
            "Помилка 500: Помилка сервера.Зверніться до системного адміністратора",
            "danger"
          )
        );
        dispatch(toggleLoader(false));
      } else if (res.response?.status === 502) {
        dispatch(
          setAlertMessageThunk(
            "Помилка 502: Невірна відповідь від шлюзу. Зачекайте кілька хвилин та спробуйте запит знову, або зверніться до системного адміністратора",
            "danger"
          )
        );
        dispatch(toggleLoader(false));
      } else if (res.response?.status === 504) {
        dispatch(
          setAlertMessageThunk(
            "Помилка 504: Затрімка відповіді від шлюзу. Зачекайте кілька хвилин та спробуйте запит знову, або зверніться до системного адміністратора",
            "danger"
          )
        );
        dispatch(toggleLoader(false));
      } else if (res && res.data.content.length === 0) {
        dispatch(
          setAlertMessageThunk("За данними пошуку збігів не знайдено", "danger")
        );
      }
      dispatch(searchResultsAC(res ? res.data.content : []));
      dispatch(setTotalElementsCount(res.data.totalElements));
      dispatch(setCurrentPageCount(res.data.number));
      dispatch(toggleLoader(false));
    });
  };

export default fisSearchReducer;
