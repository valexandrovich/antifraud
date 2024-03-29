import React, { useState } from "react";
import { sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import { Form, Formik } from "formik";
import * as Yup from "yup";
import { useSelector } from "react-redux";

const phoneSchema = Yup.object().shape({
  phone: Yup.string().matches("^(?:\\+)?(\\d{5,12})$", "Не вірний формат"),
});

const Phone = ({ data, onChange }) => {
  const { id, phone, importSources } = data;
  const [edit, setEdit] = useState(true);
  const [source, setSource] = useState(false);
  const userRole = useSelector((state) => state.auth.role);
  return (
    <>
      {edit ? (
        <div className="card mb-3">
          {userRole === "ADMIN" && (
            <div className="d-flex justify-content-end">
              <IoIcons.IoIosCreate
                className="pointer"
                style={{ fontSize: "24px", color: "#60aa18" }}
                onClick={() => setEdit(!edit)}
              />
            </div>
          )}

          <p className={"source-container ml-10"}>
            <b className="mr-10">Телефон:</b>
            {phone}
            <span
              onClick={() => setSource(!source)}
              onMouseLeave={() => setTimeout(() => setSource(false), 500)}
              className="ml-10 pointer"
            >
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
            {((source && userRole === "ADVANCED") ||
              (source && userRole === "ADMIN")) &&
              importSources.map((s) => {
                return (
                  <ul className={"source_w"} key={s.id}>
                    <li>{s.name}</li>
                  </ul>
                );
              })}
          </p>
        </div>
      ) : (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  phone: phone,
                }}
                validationSchema={phoneSchema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "Phone");
                }}
              >
                {({
                  touched,
                  errors,
                  handleBlur,
                  handleChange,
                  handleSubmit,
                  isValid,
                  values,
                }) => (
                  <Form onSubmit={handleSubmit}>
                    <div className="d-flex justify-content-end">
                      <button
                        className={"border-0"}
                        type={"submit"}
                        disabled={!isValid}
                      >
                        <IoIcons.IoIosSave
                          className="pointer"
                          style={{ fontSize: "24px", color: "#60aa18" }}
                        />
                      </button>

                      <IoIcons.IoMdTrash
                        className="pointer"
                        style={{ fontSize: "24px", color: "red" }}
                      />
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="phone">Телефон:</label>
                      <input
                        name="phone"
                        id={id}
                        className={`form-control ${
                          touched.phone && errors.phone ? "is-invalid" : ""
                        }`}
                        value={values.phone}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                        maxLength={13}
                      />

                      {errors.phone ? (
                        <span className="text-danger">{errors.phone}</span>
                      ) : null}
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Phone;
