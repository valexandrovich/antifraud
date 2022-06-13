import React from "react";
import Loader from "react-spinner-loader";

const Spinner = ({ loader, message }) => {
  return (
    <Loader className="loader" show={loader} type="box" message={message} />
  );
};

export default Spinner;
