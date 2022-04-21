import React from "react";
import "bootstrap/dist/css/bootstrap.css";
import "./styles/Style.css";
import { useSelector } from "react-redux";
import { HashRouter, Switch, Route } from "react-router-dom";

import UploadFile from "./pages/upload/UploadFile";

import ErrorPage from "./pages/ErrorPage";
import Search from "./pages/search/Search";
import Aside from "./pages/aside/Aside";
import DbCheck from "./pages/dbCheck/DbCheck";
import UploadedFiles from "./pages/uploaded_files/UploadedFiles";
import Card from "./components/Card";
import Login from "./pages/login/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import Progress from "./pages/progress/Progress";

const App = () => {
  const isAuth = useSelector((state) => state.auth.isAuth);
  const alertMsg = useSelector((state) => state.auth.alert);
  const role = useSelector((state) => state.auth.role);
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
        <HashRouter>
          {role && <Aside />}
          <Switch>
            <ProtectedRoute exact path="/" component={Search} />
            <ProtectedRoute path="/add-file" component={UploadFile} />
            <ProtectedRoute path="/search" component={Search} />
            <ProtectedRoute path="/db_check" component={DbCheck} />
            <ProtectedRoute path="/uploaded_files" component={UploadedFiles} />
            <ProtectedRoute path="/card/:id" component={Card} />
            <ProtectedRoute path="/progress" component={Progress} />
            <Route path="/error" component={ErrorPage} />
          </Switch>
        </HashRouter>
      ) : (
        <Route component={Login} />
      )}
    </>
  );
};

export default App;
