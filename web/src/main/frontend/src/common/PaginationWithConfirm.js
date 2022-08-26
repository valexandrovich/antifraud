import React, { useCallback, useEffect, useState } from "react";
import * as IoIcons from "react-icons/io";

const PaginationWithConfirm = ({
  filesPerPage,
  totalFiles,
  paginate,
  margin,
  pageNo,
  search,
  setSearch,
}) => {
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
  const handleSearch = useCallback(() => {
    setSearch(!search);
  }, [search, setSearch]);
  const keyPress = useCallback(
    (e) => {
      if (e.key === "Enter") {
        handleSearch();
      }
    },
    [handleSearch]
  );
  useEffect(() => {
    document.addEventListener("keydown", keyPress);
    return () => document.removeEventListener("keydown", keyPress);
  }, [keyPress]);
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
                      setSearch(!search);
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
                <li className="page-item mb-2">
                  <div className={"d-flex"}>
                    <label htmlFor="header-search">
                      <span className="visually-hidden">Сторінка</span>
                    </label>

                    <input
                      style={{ width: 120, outline: "none" }}
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
                      value={selected}
                      type="number"
                      placeholder="Сторінка"
                      name="search"
                      className={"form-control border-0"}
                    />
                    <button
                      className={"page-link border-0"}
                      onClick={handleSearch}
                      type="button"
                    >
                      <IoIcons.IoIosSearch />
                    </button>
                  </div>
                </li>
              )}
            </div>
          );
        })}
      </ul>
    </nav>
  );
};

export default PaginationWithConfirm;
