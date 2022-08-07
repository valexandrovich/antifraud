export const SET_ALERT_MESSAGE = "SET_ALERT_MESSAGE";
export const TOGGLE_LOADER = "TOGGLE_LOADER";
export const TOGGLE_TAB = "TOGGLE_TAB";
export const SET_FILE_ID = "SET_FILE_ID";
export const SET_JURIDICAL_FILE_ID = "SET_JURIDICAL_FILE_ID";

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

export const toggleLoader = (status) => {
  return {
    type: TOGGLE_LOADER,
    status: status,
  };
};

export const setFileID = (id) => {
  return {
    type: SET_FILE_ID,
    fileID: id,
  };
};
export const setJuridicalFileID = (id) => {
  return {
    type: SET_JURIDICAL_FILE_ID,
    juridicalFileID: id,
  };
};

export const toggleTab = (tab) => {
  return {
    type: TOGGLE_TAB,
    tab: tab,
  };
};
