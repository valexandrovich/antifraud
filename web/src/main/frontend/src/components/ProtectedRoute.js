import React from "react";
import { useSelector } from "react-redux";
import { Redirect, Route } from "react-router-dom";

const ProtectedRoute = ({ component: Component, ...restOfProps }) => {
  const role = useSelector((state) => state.auth.role);
  return (
    <Route
      {...restOfProps}
      render={(props) =>
        role === "ADVANCED" || role == null ? (
          <Component {...props} />
        ) : (
          <Redirect to="/error" />
        )
      }
    />
  );
};

export default ProtectedRoute;
