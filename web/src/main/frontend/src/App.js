import React, {useEffect} from "react";
import "bootstrap/dist/css/bootstrap.css";
import "./styles/Style.css";
import {useDispatch, useSelector} from "react-redux";
import {Switch, Route, Redirect} from "react-router-dom";
import UploadFile from "./pages/upload/UploadFile";

import ErrorPage from "./pages/ErrorPage";
import Search from "./pages/search/Search";
import Aside from "./pages/aside/Aside";
import UploadedFiles from "./pages/uploaded_files/UploadedFiles";
import Login from "./pages/login/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import Progress from "./pages/progress/Progress";
import Sheduler from "./pages/sheduler/Sheduler";
import SingleCard from "./pages/card/SingleCard";
import Monitoring from "./pages/monitoring/Monitoring";
import {checkAutoLogin} from './api/AuthApi';

const App = () => {
    const isAuth = useSelector((state) => state.auth.isAuth);
    const alertMsg = useSelector((state) => state.auth.alert);
    const dispatch = useDispatch();
    const token = localStorage.getItem("user");
    useEffect(() => {
        checkAutoLogin(dispatch);
    }, [dispatch, token]);
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
                    <Aside/>
                    <Switch>
                        <ProtectedRoute exact path="/add-file" component={UploadFile}/>
                        <Route exact path="/">
                            <Redirect exact to="/search" component={Search}/>
                        </Route>
                        <Route exact path="/search" component={Search}/>
                        <ProtectedRoute
                            exact
                            path="/uploaded_files"
                            component={UploadedFiles}
                        />
                        <Route exact path="/card/:id" component={SingleCard}/>
                        <ProtectedRoute exact path="/progress" component={Progress}/>
                        <ProtectedRoute exact path="/sheduler" component={Sheduler}/>
                        <Route exact path="/subscription" component={Monitoring}/>
                        <Route path="/error" component={ErrorPage}/>
                    </Switch>
                </>
            ) : (
                <Login/>
            )}
        </>
    );
};

export default App;
