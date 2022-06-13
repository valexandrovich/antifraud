import React from "react";
import { DateObject } from "react-multi-date-picker";
import { sourceName } from "./Card";

const Tags = ({ data }) => {
  const { name, asOf, until, source, importSources } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">Тег:</b>
        {name}
        <span className="ml-10">
          {importSources && importSources.length > 0
            ? `(${importSources.length} ${sourceName(importSources)})`
            : ""}
        </span>
      </p>
      <div className="d-flex">
        <span className="mr-10">
          <b className="mr-10">З:</b>
          {new DateObject(asOf).format("DD.MM.YYYY")}
        </span>
        <span>
          <b className="mr-10">По:</b>
          {until === null
            ? "(Теперішній)"
            : new DateObject(until).format("DD.MM.YYYY")}
        </span>
      </div>
      <small>{source}</small>
    </div>
  );
};

export default Tags;
