import React from "react";

const PerPage = ({ pageSize, setPageSize, setPageNo }) => {
  return (
    <div className="d-flex justify-content-end">
      <div className="form-group col-md-2 mb-2">
        <label htmlFor="pageSize">Показувати по:</label>
        <select
          className="form-select"
          name="pageSize"
          value={pageSize}
          onChange={(e) => {
            setPageSize(e.target.value);
            setPageNo(0);
          }}
        >
          <option value={6}>6</option>
          <option value={12}>12</option>
          <option value={24}>24</option>
        </select>
      </div>
    </div>
  );
};

export default PerPage;
