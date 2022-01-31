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

const App = () => {
  const isAuth = useSelector((state) => state.auth.isAuth);

  return (
    <>
      {isAuth ? (
        <HashRouter>
          <Aside />
          <Switch>
            <Route exact path="/" component={Search} />
            <Route path="/add-file" component={UploadFile} />
            <Route path="/search" component={Search} />
            <Route path="/db_check" component={DbCheck} />
            <Route path="/uploaded_files" component={UploadedFiles} />
            <Route path="/card/:id" component={<Card />} />
            <Route component={ErrorPage} />
          </Switch>
        </HashRouter>
      ) : (
        <Route component={Login} />
      )}
    </>
  );
};

export default App;
