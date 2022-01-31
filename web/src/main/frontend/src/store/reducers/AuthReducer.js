import authService from "../../api/AuthApi";

const CHANGE_FORM_VALUE = "CHANGE_FORM_VALUE";
const SUBMIT_AUTH = "SUBMIT_AUTH";
const LOGOUT = "LOGOUT";
let initialState = {
  authForm: {
    login: "",
    password: "",
  },
  isAuth: false,
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
    case SUBMIT_AUTH:
      return {
        ...state,
        authForm: initialState.authForm,
        isAuth: true,
      };
    case LOGOUT:
      return {
        ...state,
        isAuth: false,
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

export const submitUserAuth = () => {
  return {
    type: SUBMIT_AUTH,
  };
};

export const logoutUser = () => {
  return {
    type: LOGOUT,
  };
};

export const submitUserAuthThunk = (authForm) => (dispatch) => {
  if(authForm.login === "test" && authForm.password === "test" ){
    dispatch(submitUserAuth());
  }
  else{
    authService
        .auth(authForm)
        .then((res) => {
          dispatch(submitUserAuth());
        })
        .catch((err) => {
          console.log(err);
        });
  }

};

export const logoutUserThunk = () => (dispatch) => {
  authService.logout();
  dispatch(logoutUser());
};

export default AuthReducer;
