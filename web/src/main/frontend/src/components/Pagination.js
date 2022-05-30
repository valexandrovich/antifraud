import React, { useState } from "react";

const Pagination = ({ filesPerPage, totalFiles, paginate }) => {
  const pageNumbers = [];
  const [selected, setSelected] = useState(1);
  for (let i = 1; i <= Math.ceil(totalFiles / filesPerPage); i++) {
    pageNumbers.push(i);
  }

  return (
    <nav>
      <ul className="pagination flex-wrap align-items-center">
        {pageNumbers.map((number) => (

          <li key={number} className="page-item mb-2">
            <button onClick={() => { paginate(number); setSelected(number); }} className="page-link">
              <span className={selected === number ? "paginate-active" : null}>{number}</span>
            </button>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default Pagination;
