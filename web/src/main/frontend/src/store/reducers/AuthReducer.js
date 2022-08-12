import authService from "../../api/AuthApi";
import jwt_decode from "jwt-decode";

import {
  SET_ALERT_MESSAGE,
  SET_FILE_ID,
  SET_JURIDICAL_FILE_ID,
  setAlertMessageThunk,
  TOGGLE_TAB,
} from "./actions/Actions";

import { resetFormValueAc } from "./actions/YPersonActions";
import { resetYCompanyFormValueAc } from "./actions/YcompanyActions";

export const CHANGE_FORM_VALUE = "CHANGE_FORM_VALUE";
export const LOGOUT = "LOGOUT";
export const SET_AUTH = "SET_AUTH";
export const SUBMIT_AUTH = "SUBMIT_AUTH";

let initialState = {
  authForm: {
    login: "",
    password: "",
  },
  isAuth: false,
  role: null,
  userName: null,
  alert: {
    active: false,
    message: "",
    type_message: "",
  },
  activeTab: "fiz",
  fileID: null,
  juridicalFileID: null,
  monitoring: false,
};

const AuthReducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case CHANGE_FORM_VALUE:
      let authForm = { ...state.authForm };
      authForm[action.input] = action.value;
      return {
        ...state,
        authForm: authForm,
      };
    case SET_AUTH:
      return {
        ...state,
        isAuth: true,
        role: action.role,
      };
    case SET_FILE_ID: {
      return {
        ...state,
        fileID: action.fileID,
      };
    }
    case SET_JURIDICAL_FILE_ID: {
      return {
        ...state,
        juridicalFileID: action.juridicalFileID,
      };
    }
    case SUBMIT_AUTH:
      return {
        ...state,
        authForm: initialState.authForm,
        isAuth: true,
        role: action.role,
        userName: action.userName,
      };
    case LOGOUT:
      return {
        ...state,
        isAuth: false,
        role: null,
        userName: null,
      };
    case TOGGLE_TAB: {
      return {
        ...state,
        activeTab: action.tab,
      };
    }
    case SET_ALERT_MESSAGE:
      return {
        ...state,
        alert: {
          active: action.active,
          message: action.message,
          type_message: action.type_message,
        },
      };
    default:
      return state;
  }
};

export const changeFormValueAC = (input, value) => {
  return {
    type: CHANGE_FORM_VALUE,
    input: input,
    value: value,
  };
};

export const submitUserAuth = (role, userName) => {
  return {
    type: SUBMIT_AUTH,
    role: role,
    userName: userName,
  };
};

export const logoutUser = () => {
  return {
    type: LOGOUT,
  };
};
export const authenticate = () => {
  let decoded = jwt_decode(localStorage.getItem("user"));
  return {
    type: SUBMIT_AUTH,
    role: decoded.role,
  };
};

export const submitUserAuthThunk = (authForm) => (dispatch) => {
  authService
    .auth(authForm)
    .then((res) => {
      if (res.data.role == null) {
        dispatch(
          setAlertMessageThunk(
            "Недостатньо прав зверніться до адміністратора",
            "danger"
          )
        );
      }
      dispatch(submitUserAuth(res.data.role, res.data.userName));
    })
    .catch((err) => {
      dispatch(setAlertMessageThunk(err.response.data.message, "danger"));
    });
};

export const logoutUserThunk = () => (dispatch) => {
  authService.logout();
  dispatch(logoutUser());
  dispatch(resetFormValueAc());
  dispatch(resetYCompanyFormValueAc());
};

export default AuthReducer;
