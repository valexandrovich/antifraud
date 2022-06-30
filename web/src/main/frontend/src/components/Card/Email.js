import React, { useState } from "react";
import { sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import { Form, Formik } from "formik";
import * as Yup from "yup";
import { useSelector } from "react-redux";

const emailSchema = Yup.object().shape({
  email: Yup.string().email("Невірний email"),
});
const Email = ({ data, onChange }) => {
  const { id, email, importSources } = data;
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
            <b className="mr-10">Email:</b>
            {email}
            <span className="ml-10">
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
          </p>
        </div>
      ) : (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  email: email,
                }}
                validationSchema={emailSchema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "Email");
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
                      <label htmlFor="email">Email:</label>
                      <input
                        name="email"
                        id={id}
                        className={`form-control ${
                          touched.email && errors.email ? "is-invalid" : ""
                        }`}
                        value={values.email}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.email ? (
                        <span className="text-danger">{errors.email}</span>
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

export default Email;
