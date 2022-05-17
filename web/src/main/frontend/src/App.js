import React from "react";
import "bootstrap/dist/css/bootstrap.css";
import "./styles/Style.css";
import { useSelector } from "react-redux";
import { Switch, Route, Redirect } from "react-router-dom";
import UploadFile from "./pages/upload/UploadFile";

import ErrorPage from "./pages/ErrorPage";
import Search from "./pages/search/Search";
import Aside from "./pages/aside/Aside";
import UploadedFiles from "./pages/uploaded_files/UploadedFiles";
import Login from "./pages/login/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import Progress from "./pages/progress/Progress";
import Sheduler from "./pages/sheduler/Sheduler";
import SingleCard from "./pages/aside/card/SingleCard";

const App = () => {
  const isAuth = useSelector((state) => state.auth.isAuth);
  const alertMsg = useSelector((state) => state.auth.alert);
  return (
    <>
      {alertMsg.message && (
        <div className={`row fixed-top bg-${alertMsg.type_message}`}>
          <div className="container warning-block">
            <p className="text-white text-center fs-2">{alertMsg.message}</p>
          </div>
        </div>
      )}
      {isAuth ? (
        <>
          <Aside />
          <Switch>
            <ProtectedRoute exact path="/add-file" component={UploadFile} />
            <Route exact path="/">
              <Redirect exact to="/search" component={Search} />
            </Route>
            <Route exact path="/search" component={Search} />
            <ProtectedRoute
              exact
              path="/uploaded_files"
              component={UploadedFiles}
            />
            <ProtectedRoute exact path="/card/:id" component={SingleCard} />
            <ProtectedRoute exact path="/progress" component={Progress} />
            <ProtectedRoute exact path="/sheduler" component={Sheduler} />

            <Route path="/error" component={ErrorPage} />
          </Switch>
        </>
      ) : (
        <Login />
      )}
    </>
  );
};

export default App;
