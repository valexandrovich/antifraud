import React from "react";
import { DateObject } from "react-multi-date-picker";
const Tags = ({ data }) => {
  const { name, asOf, until, source } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">Тег:</b>
        {name}
      </p>
      <div className="d-flex">
        <span className="mr-10">
          <b className="mr-10">З:</b>
          {new DateObject(asOf).format("DD.MM.YYYY")}
        </span>
        <span>
          <b className="mr-10">По:</b>
          {until === null ? "(Теперішній)" : new DateObject(until).format("DD.MM.YYYY")}
        </span>
      </div>
      <small>{source}</small>
    </div>
  );
};

export default Tags;
