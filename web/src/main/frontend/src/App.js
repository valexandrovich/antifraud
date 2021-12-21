import React, { Component } from "react";
import "bootstrap/dist/css/bootstrap.css";
import "./styles/Style.css";

import { BrowserRouter, Switch, Route } from "react-router-dom";

import UploadFile from "./pages/upload/UploadFile";

import ErrorPage from "./pages/ErrorPage";
import Search from "./pages/search/Search";
import Aside from "./pages/aside/Aside";
import DbCheck from "./pages/dbCheck/DbCheck";
import UploadedFiles from "./pages/uploaded_files/UploadedFiles";

class App extends Component {
  render() {
    return (
      <>
        <BrowserRouter>
          <Aside />
          <Switch>
            <Route exact path="/" component={Search} />
            <Route path="/add-file" component={UploadFile} />
            <Route path="/search" component={Search} />
            <Route path="/db_check" component={DbCheck} />
            <Route path="/uploaded_files" component={UploadedFiles} />

            <Route component={ErrorPage} />
          </Switch>
        </BrowserRouter>
      </>
    );
  }
}

export default App;
