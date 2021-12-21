import React from "react";

const Pagination = ({ filesPerPage, totalFiles, paginate }) => {
  const pageNumbers = [];

  for (let i = 1; i <= Math.ceil(totalFiles / filesPerPage); i++) {
    pageNumbers.push(i);
  }

  return (
    <nav>
      <ul className="pagination">
        {pageNumbers.map((number) => (
          <li key={number} className="page-item mb-2">
            <a onClick={() => paginate(number)} className="page-link ">
              {number}
            </a>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default Pagination;
