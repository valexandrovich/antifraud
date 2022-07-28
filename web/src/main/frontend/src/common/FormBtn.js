import React from "react";

const FormBtn = ({ disabled, searchAction, clearAction }) => {
  return (
    <div className="row mt-3">
      <div className="form-group col-md-3 mb-3">
        <button
          onClick={searchAction}
          disabled={disabled}
          type="submit"
          className="btn custom-btn w-100"
        >
          Пошук
        </button>
      </div>
      <div className="form-group col-md-3">
        <button
          onClick={clearAction}
          type="button"
          className="btn btn-danger w-100"
        >
          Очистити
        </button>
      </div>
    </div>
  );
};

export default FormBtn;
