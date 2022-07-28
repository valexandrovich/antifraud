import React, { useState } from "react";
import { useSelector } from "react-redux";
import * as IoIcons from "react-icons/io";
import { sourceName } from "../YPersonCard/Card";
import { Form, Formik } from "formik";
import * as Yup from "yup";

const altSchema = Yup.object().shape({
  name: Yup.string().min(3, " Мін 3 cимвола").max(255, "Макс 255 cимволів"),
});

const AltCompanies = ({ data, onChange }) => {
  const { id, name, language, importSources } = data;
  const [edit, setEdit] = useState(true);
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
          <p className={"ml-10"}>
            <b className="mr-10">Назва:</b>
            {name}
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
                  name: data.name,
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
                    <div className="col-sm-9 mr-10">
                      <label htmlFor="lastName">Ім'я:</label>
                      <input
                        name="name"
                        id={id}
                        className={`form-control ${
                          touched.name && errors.name ? "is-invalid" : ""
                        }`}
                        value={values.name}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.name ? (
                        <span className="text-danger">{errors.name}</span>
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

export default AltCompanies;
