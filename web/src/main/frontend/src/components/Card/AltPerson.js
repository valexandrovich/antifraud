import React, { useState } from "react";
import { sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import { Form, Formik } from "formik";
import * as Yup from "yup";
import { useSelector } from "react-redux";

const altSchema = Yup.object().shape({
  firstName: Yup.string()
    .min(3, " Мін 3 cимвола")
    .max(255, "Макс 255 cимволів"),
  lastName: Yup.string().min(3, " Мін 3 cимвола").max(255, "Макс 255 cимволів"),
  patName: Yup.string().min(3, " Мін 3 cимвола").max(255, "Макс 255 cимволів"),
});

const AltPerson = ({ data, onChange }) => {
  const { id, lastName, firstName, patName, language, importSources } = data;
  const [edit, setEdit] = useState(true);
  const canEdit = useSelector((state) => state.auth.canEdit);
  return (
    <>
      {edit ? (
        <div className="card mb-3">
          {canEdit && (
            <div className="d-flex justify-content-end">
              <IoIcons.IoIosCreate
                className="pointer"
                style={{ fontSize: "24px", color: "#60aa18" }}
                onClick={() => setEdit(!edit)}
              />
            </div>
          )}
          <p className={"ml-10"}>
            <b className="mr-10">ПІБ:</b>
            {lastName}
            {""} {firstName}
            {""} {patName}
            <span className="ml-10">
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
          </p>
          <p className={"ml-10"}>{language}</p>
        </div>
      ) : (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  lastName: lastName,
                  firstName: firstName,
                  patName: patName,
                }}
                validationSchema={altSchema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "Alt");
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
                      <label htmlFor="lastName">Прізвище:</label>
                      <input
                        name="lastName"
                        id={id}
                        className={`form-control ${
                          touched.lastName && errors.lastName
                            ? "is-invalid"
                            : ""
                        }`}
                        value={values.lastName}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.lastName ? (
                        <span className="text-danger">{errors.lastName}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="lastName">Ім'я:</label>
                      <input
                        name="firstName"
                        id={id}
                        className={`form-control ${
                          touched.firstName && errors.firstName
                            ? "is-invalid"
                            : ""
                        }`}
                        value={values.firstName}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.firstName ? (
                        <span className="text-danger">{errors.firstName}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="patName">Побатькові:</label>
                      <input
                        name="patName"
                        id={id}
                        className={`form-control ${
                          touched.patName && errors.patName ? "is-invalid" : ""
                        }`}
                        value={values.patName}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.patName ? (
                        <span className="text-danger">{errors.patName}</span>
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

export default AltPerson;
