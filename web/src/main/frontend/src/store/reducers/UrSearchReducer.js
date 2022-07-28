import {
  setAlertMessageThunk,
  TOGGLE_LOADER,
  toggleLoader,
} from "./actions/Actions";
import YCompanyApi from "../../api/YCompanyApi";
import {
  RESET_YCOMPANY_FORM_VALUES,
  searchResultsYCompanyAC,
  SET_CURRENT_PAGE_YCOMPANY,
  SET_PER_PAGE_YCOMPANY,
  SET_SEARCH_YCOMPANY_RESULTS,
  SET_TOTAL_YCOMPANY_ELEMENTS,
  SET_YCOMPANY_FORM_VALUE,
  setCurrentPageYcompanyCount,
  setTotalElementsYCompanyCount,
} from "./actions/YcompanyActions";

const initialState = {
  urForm: {
    edrpou: "",
    name: "",
    pdv: "",
    address: "",
  },
  searchResults: [],
  loader: false,
  currentPage: 0,
  perPage: 6,
  totalElements: null,
};

const urSearchReducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case SET_YCOMPANY_FORM_VALUE:
      let urForm = { ...state.urForm };
      urForm[action.input] = action.value;
      return {
        ...state,
        urForm: urForm,
      };
    case RESET_YCOMPANY_FORM_VALUES: {
      return {
        ...state,
        urForm: {
          edrpou: "",
          name: "",
          pdv: "",
          address: "",
        },
        searchResults: [],
        currentPage: initialState.currentPage,
      };
    }
    case SET_SEARCH_YCOMPANY_RESULTS: {
      return {
        ...state,
        searchResults: action.search,
      };
    }
    case SET_PER_PAGE_YCOMPANY: {
      return {
        ...state,
        perPage: action.count,
      };
    }
    case SET_CURRENT_PAGE_YCOMPANY: {
      return {
        ...state,
        currentPage: action.page,
      };
    }

    case SET_TOTAL_YCOMPANY_ELEMENTS: {
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

export const setSearchYCompaniesDataThunk =
  (fizForm, pageNo, pageSize) => (dispatch) => {
    dispatch(toggleLoader(true));
    YCompanyApi.search(fizForm, pageNo, pageSize).then((res) => {
      if (res.response?.status === 500) {
        dispatch(
          setAlertMessageThunk(
            "Помилка 500: Помилка сервера.Зверніться до системного адміністратора",
            "danger"
          )
        );
        dispatch(toggleLoader(false));
      } else if (res && res.data?.content?.length === 0) {
        dispatch(
          setAlertMessageThunk("За данними пошуку збігів не знайдено", "danger")
        );
      }
      dispatch(searchResultsYCompanyAC(res ? res.data.content : []));

      dispatch(setTotalElementsYCompanyCount(res.data.totalElements));
      dispatch(setCurrentPageYcompanyCount(res.data.number));
      dispatch(toggleLoader(false));
    });
  };

export default urSearchReducer;
