import { applyMiddleware, combineReducers, createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import thunk from "redux-thunk";
import AuthReducer from "./reducers/AuthReducer";
import fisSearchReducer from "./reducers/FizSearchReducer";
import urSearchReducer from "./reducers/UrSearchReducer";

const reducers = combineReducers({
  auth: AuthReducer,
  fiz: fisSearchReducer,
  ur: urSearchReducer,
});

const store = createStore(
  reducers,
  composeWithDevTools(applyMiddleware(thunk))
);

export default store;
