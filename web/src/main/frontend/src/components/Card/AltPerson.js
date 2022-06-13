import React from "react";
import { sourceName } from "./Card";

const AltPerson = ({ data }) => {
  const { lastName, firstName, patName, language, importSources } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">ПІБ:</b>
        {lastName}
        {""} {firstName}
        {""} {patName}
        <span className="ml-10">
          {importSources && importSources.length > 0
            ? `(${importSources.length} ${sourceName(importSources)})`
            : ""}
        </span>
      </p>
      {language}
    </div>
  );
};

export default AltPerson;
