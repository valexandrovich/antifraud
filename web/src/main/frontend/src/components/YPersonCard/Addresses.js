import React, { useState } from "react";
import { sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import { Form, Formik } from "formik";
import * as Yup from "yup";
import { useSelector } from "react-redux";

const addressSchema = Yup.object().shape({
  address: Yup.string().min(3, " Мін 3 cимвола").max(255, "Макс 255 cимволів"),
});
const Addresses = ({ data, onChange }) => {
  const { id, address, importSources } = data;
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
            <b className="mr-10">Адреса:</b>
            {address}
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
                  address: address,
                }}
                validationSchema={addressSchema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "address");
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
                    <div className="col-sm-12 mr-10">
                      <label htmlFor="address">Адреса:</label>
                      <input
                        name="address"
                        id={id}
                        className={`form-control ${
                          touched.address && errors.address ? "is-invalid" : ""
                        }`}
                        value={values.address}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        maxLength={255}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.address ? (
                        <span className="text-danger">{errors.address}</span>
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

export default Addresses;
