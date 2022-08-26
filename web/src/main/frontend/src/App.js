import React, { useEffect } from "react";
import "bootstrap/dist/css/bootstrap.css";
import "./styles/Style.css";
import { useDispatch, useSelector } from "react-redux";
import {
  Switch,
  Route,
  Redirect,
  useHistory,
  useLocation,
} from "react-router-dom";
import UploadFile from "./pages/upload/UploadFile";
import ErrorPage from "./pages/ErrorPage";
import Search from "./pages/search/Search";
import Aside from "./pages/aside/Aside";
import UploadedFiles from "./pages/uploaded_files/UploadedFiles";
import Login from "./pages/login/Login";
import ProtectedRoute from "./common/ProtectedRoute";
import Progress from "./pages/progress/Progress";
import Sheduler from "./pages/sheduler/Sheduler";
import SingleYPersonCard from "./pages/card/SingleYPersonCard";
import Monitoring from "./pages/monitoring/Monitoring";
import { checkAutoLogin } from "./api/AuthApi";
import NonFound from "./pages/NonFound";
import SingleYCompanyCard from "./pages/card/SingleYCompanyCard";
import Relations from "./pages/relations/Relations";
import withClearCache from "./common/ClearCache";

const logOutTimeout = { timeout: null };

const MainApp = () => {
  const isAuth = useSelector((state) => state.auth.isAuth);
  const alertMsg = useSelector((state) => state.auth.alert);
  const dispatch = useDispatch();
  const userRole = useSelector((state) => state.auth.role);
  const history = useHistory();
  let location = useLocation();

  useEffect(() => {
    checkAutoLogin(dispatch, history, logOutTimeout, location.pathname);
    return checkAutoLogin(dispatch, history, logOutTimeout, location.pathname);
  }, [dispatch, history, location.pathname, isAuth]);

  return (
    <>
      {alertMsg.message && (
        <div className={`row fixed-top bg-${alertMsg.type_message}`}>
          <div className="container warning-block">
            <p className="text-white text-center fs-2">{alertMsg.message}</p>
          </div>
        </div>
      )}
      {isAuth && userRole !== null ? (
        <>
          <Aside />
          <Switch>
            <Route path="/error" component={ErrorPage} />
            <Route exact path="/">
              <Redirect exact to="/search" component={Search} />
            </Route>
            <Route exact path="/search" component={Search} />
            <Route exact path="/" component={Search} />
            <ProtectedRoute
              exact
              path="/uploaded_files"
              component={UploadedFiles}
            />
            <Route exact path="/YPerson/:id" component={SingleYPersonCard} />
            <Route exact path="/YCompany/:id" component={SingleYCompanyCard} />
            <ProtectedRoute exact path="/add-file" component={UploadFile} />
            <ProtectedRoute exact path="/progress" component={Progress} />
            <ProtectedRoute exact path="/sheduler" component={Sheduler} />
            {/*<ProtectedRoute exact path="/subscription" component={Monitoring} />*/}
            <Route path="/relations" component={Relations} />
            <Route component={NonFound} />
          </Switch>
        </>
      ) : (
        <Login />
      )}
    </>
  );
};
const ClearCacheComponent = withClearCache(MainApp);

function App() {
  return <ClearCacheComponent />;
}

export default App;
