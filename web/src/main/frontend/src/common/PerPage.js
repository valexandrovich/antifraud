import React from "react";
import {
  setCurrentPageCount,
  setPerPageCount,
} from "../store/reducers/actions/YPersonActions";
import { useDispatch } from "react-redux";
import {
  setCurrentPageYcompanyCount,
  setPerPageYCompanyCount,
} from "../store/reducers/actions/YcompanyActions";

const PerPage = ({ pageSize, setPageNo, setPageSize, type }) => {
  const dispatch = useDispatch();
  const setPageVal = (type, e) => {
    if (type === "fiz") {
      dispatch(setPerPageCount(Number(e.target.value)));
      dispatch(setCurrentPageCount(0));
    }
    if (type === "ur") {
      dispatch(setPerPageYCompanyCount(Number(e.target.value)));
      dispatch(setCurrentPageYcompanyCount(0));
    }
  };
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
            setPageVal(type, e);
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
