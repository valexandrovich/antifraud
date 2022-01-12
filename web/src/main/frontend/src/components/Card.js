import React from "react";

const Card = ({ data }) => {
  return (
    <>
      <div className="card m-3">
        <div className="card-header">{data.uuid.uuid}</div>
        <div className="card-body">
          <h5 className="card-title">{data.nameUk}</h5>
          <h5 className="card-title">{data.surnameRu}</h5>
          <p className="card-text">{data.inn}</p>

        </div>
        <button className="btn btn-success mb-3">Деталі</button>
      </div>
    </>
  );
};

export default Card;
