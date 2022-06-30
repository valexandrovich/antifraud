import React, { useState } from "react";
import { pad, sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { useSelector } from "react-redux";

const innSchema = Yup.object().shape({
  inn: Yup.string().matches("^[0-9]{10}$", "Невірний ІПН"),
});

const Inn = ({ data, onChange }) => {
  const { id, inn, importSources } = data;
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
            <b className="mr-10">ІПН:</b>{" "}
            {inn.toString.length < 10 ? pad(inn, 10) : inn}
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
                  inn: inn.toString.length < 10 ? pad(inn, 10) : inn,
                }}
                validationSchema={innSchema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "INN");
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
                      <label htmlFor="inn">ІПН:</label>
                      <input
                        name="inn"
                        id={id}
                        className={`form-control ${
                          touched.inn && errors.inn ? "is-invalid" : ""
                        }`}
                        value={values.inn}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.inn ? (
                        <span className="text-danger">{errors.inn}</span>
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

export default Inn;
