import React from "react";

const ProgressBar = (props) => {
  const { bgcolor, completed } = props;

  const containerStyles = {
    height: 20,
    width: "150px",
    borderRadius: 50,
    margin: 50,
  };

  const fillerStyles = {
    minWidth: "40px",
    width: `${completed <= 100 ? completed + "%" : "100%"}`,
    backgroundColor: bgcolor,
    borderRadius: "inherit",
    textAlign: "right",
  };

  const labelStyles = {
    padding: 5,
    color: "white",
    fontWeight: "bold",
  };

  return (
    <td style={containerStyles}>
      <div style={fillerStyles}>
        <span style={labelStyles}>
          {completed <= 100 ? completed + "%" : ""}
        </span>
      </div>
    </td>
  );
};

export default ProgressBar;
