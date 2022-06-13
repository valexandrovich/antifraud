import React from "react";
import { sourceName } from "./Card";

const Addresses = ({ data }) => {
  const { address, importSources } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">Адреса:</b>
        {address}
        <span className="ml-10">
          {importSources && importSources.length > 0
            ? `(${importSources.length} ${sourceName(importSources)})`
            : ""}
        </span>
      </p>
    </div>
  );
};

export default Addresses;
