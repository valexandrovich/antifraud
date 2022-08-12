import React from "react";
import { Field } from "formik";

const FloatInput = ({
  name,
  val,
  label,
  col = "col-sm-4",
  type = "text",
  onChange,
  handleBlur,
  touched,
  errors,
  max,
  ...rest
}) => {
  return (
    <div className={`form-group ${col}`}>
      <span className="has-float-label">
        <Field
          name={name}
          onChange={onChange}
          onBlur={handleBlur}
          value={val}
          className={`form-control ${touched && errors ? "is-invalid" : ""}`}
          id={name}
          maxLength={max}
          type={type}
          placeholder={label}
          {...rest}
        />
        {errors ? (
          <span className="text-danger">{errors}</span>
        ) : (
          <label htmlFor={name}>{label}</label>
        )}
      </span>
    </div>
  );
};

export default FloatInput;
