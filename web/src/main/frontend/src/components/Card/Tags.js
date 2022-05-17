import React from "react";

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
          {asOf}
        </span>
        <span>
          <b className="mr-10">По:</b>
          {until === null ? "(Теперішній)" : until}
        </span>
      </div>
    </div>
  );
};

export default Tags;
