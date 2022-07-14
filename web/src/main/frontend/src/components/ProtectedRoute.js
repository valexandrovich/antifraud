import React from "react";
import { useSelector } from "react-redux";
import { Route } from "react-router-dom";
import ErrorPage from "../pages/ErrorPage";

const ProtectedRoute = ({ component: Component, ...restOfProps }) => {
  const userRole = useSelector((state) => state.auth.role);

  return (
    <Route
      {...restOfProps}
      render={(props) =>
        userRole === "ADVANCED" || userRole === "ADMIN" || userRole == null ? (
          <Component {...props} />
        ) : (
          <ErrorPage />
        )
      }
    />
  );
};

export default ProtectedRoute;
