import React, { useState } from "react";

const Pagination = ({ filesPerPage, totalFiles, paginate }) => {
  const pageNumbers = [];
  const [selected, setSelected] = useState(1);
  if (totalFiles / filesPerPage <= 15) {
    for (let i = 1; i <= Math.ceil(totalFiles / filesPerPage); i++) {
      pageNumbers.push(i);
    }
  } else {
    pageNumbers.push(
      1,
      2,
      3,
      "...",
      totalFiles - 2,
      totalFiles - 1,
      totalFiles
    );
  }
  return (
    <nav className={"d-flex align-items-center justify-content-center"}>
      <ul className="pagination flex-wrap align-items-center">
        {pageNumbers.map((number, idx) => {
          return (
            <div key={number}>
              {typeof number === "number" ? (
                <li key={number + idx} className="page-item mb-2">
                  <button
                    type={"button"}
                    onClick={() => {
                      paginate(number);
                      setSelected(number);
                    }}
                    className="page-link"
                  >
                    <span
                      className={selected === number ? "paginate-active" : null}
                    >
                      {number}
                    </span>
                  </button>
                </li>
              ) : (
                <li style={{ width: 120 }} className="page-item mb-2">
                  <label className={"d-flex"}>
                    ...
                    <input
                      className={"form-control"}
                      onChange={(e) => {
                        setSelected(
                          e.target.value > totalFiles || e.target.value < 1
                            ? totalFiles
                            : Number(e.target.value)
                        );
                        paginate(selected);
                      }}
                      type={"number"}
                      value={selected}
                    />
                    ...
                  </label>
                </li>
              )}
            </div>
          );
        })}
      </ul>
    </nav>
  );
};

export default Pagination;
