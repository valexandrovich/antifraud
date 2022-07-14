import React, { useEffect, useState } from "react";

const Pagination = ({ filesPerPage, totalFiles, paginate, reset, margin }) => {
  const pageNumbers = [];
  const [selected, setSelected] = useState(1);
  useEffect(() => {
    setSelected(reset + 1 || selected);
  }, [filesPerPage, pageNumbers, reset, selected, totalFiles]);
  if (totalFiles / filesPerPage <= 4) {
    for (let i = 1; i <= Math.ceil(totalFiles / filesPerPage); i++) {
      pageNumbers.push(i);
    }
  } else {
    pageNumbers.push(
      1,
      2,
      3,
      "...",
      Math.ceil(totalFiles / filesPerPage - 2),
      Math.ceil(totalFiles / filesPerPage - 1),
      Math.ceil(totalFiles / filesPerPage)
    );
  }
  return (
    <nav
      style={{ marginTop: margin + "px" }}
      className={"d-flex align-items-center justify-content-center"}
    >
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
                          e.target.value > totalFiles / filesPerPage ||
                            e.target.value < 1
                            ? Math.ceil(totalFiles / filesPerPage)
                            : Number(e.target.value)
                        );
                        paginate(
                          e.target.value > totalFiles / filesPerPage ||
                            e.target.value < 1
                            ? Math.ceil(totalFiles / filesPerPage)
                            : Number(e.target.value)
                        );
                      }}
                      type={"number"}
                      value={selected}
                      max={Math.ceil(totalFiles / filesPerPage)}
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
