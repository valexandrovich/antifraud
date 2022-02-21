import React from "react";

const Card = ({ data }) => {
  const { id, lastName, firstName, patName, inns, birthdate } = data;
  return (
    <>
      <div className="card m-3">
        <div className="card-header">{id}</div>
        <div className="card-body">
          <p>
            <b>
              {lastName} {""}
              {firstName} {""}
              {patName}
            </b>
          </p>
          <p>
            Д.Н.:{birthdate}; ІПН: {inns[0]?.inn}
          </p>
        </div>
      </div>
    </>
  );
};

export default Card;
