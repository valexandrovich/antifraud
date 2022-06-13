import React from "react";
import { pad, sourceName } from "./Card";

const Inn = ({ data }) => {
  const { inn, importSources } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">ІПН:</b>{" "}
        {inn.toString.length < 10 ? pad(inn, 10) : inn}
        <span className="ml-10">
          {importSources && importSources.length > 0
            ? `(${importSources.length} ${sourceName(importSources)})`
            : ""}
        </span>
      </p>
    </div>
  );
};

export default Inn;
