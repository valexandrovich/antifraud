import authService from "../../api/AuthApi";
import jwt_decode from "jwt-decode";

const CHANGE_FORM_VALUE = "CHANGE_FORM_VALUE";
const SUBMIT_AUTH = "SUBMIT_AUTH";
const LOGOUT = "LOGOUT";
const SET_ALERT_MESSAGE = "SET_ALERT_MESSAGE";
const SET_AUTH = "SET_AUTH";
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
      if (res.data.role === null) {
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
};

export const setAlertMessage = (active, message, type_message) => {
  return {
    type: SET_ALERT_MESSAGE,
    active: active,
    message: message,
    type_message: type_message,
  };
};

export const setAlertMessageThunk = (message, type_message) => (dispatch) => {
  dispatch(setAlertMessage(true, message, type_message));
  setTimeout(function () {
    dispatch(setAlertMessage(false, "", ""));
  }, 5500);
};

export default AuthReducer;
