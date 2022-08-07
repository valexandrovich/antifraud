import React, { useEffect, useState } from "react";

const Pagination = ({ filesPerPage, totalFiles, paginate, margin, pageNo }) => {
  const pageNumbers = [];
  const [selected, setSelected] = useState(1);
  const selectedPage =
    Math.ceil(totalFiles / filesPerPage) < selected ? 1 : selected;
  useEffect(() => {
    setSelected(
      pageNo < Math.ceil(totalFiles / filesPerPage) ? pageNo + 1 : selectedPage
    );
  }, [filesPerPage, pageNo, pageNumbers, selected, selectedPage, totalFiles]);
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
            <div key={idx}>
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
                      {number ? number : ""}
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
