import React, { useState } from "react";
import { Formik, Form } from "formik";
import * as Yup from "yup";

const urSchema = Yup.object().shape({
  edrpo: Yup.string().matches("^[0-9]{7,15}$", "7-15"),
  cPhone: Yup.string().matches("^(?:\\+38)?(\\d{10})$", "Не вірний формат"),
  cName: Yup.string().max(255, "Занадто довге"),
  cAdress: Yup.string().max(255, "Занадто довге"),
});

const UrFormSearch = () => {
  const [searchFormUr, setSearchFormUr] = useState({
    edrpo: "",
    cName: "",
    cAdress: "",
    cPhone: "",
  });

  const handleInputChange = (e) => {
    setSearchFormUr({ ...searchFormUr, [e.target.name]: e.target.value });
  };
  return (
    <Formik
      initialValues={{
        edrpo: "",
        cName: "",
        cAdress: "",
        cPhone: "",
      }}
      validationSchema={urSchema}
      onSubmit={({ setSubmitting }) => {
        setSubmitting(false);
      }}
    >
      {({
        touched,
        errors,
        isSubmitting,
        resetForm,
        handleBlur,
        handleChange,
        values,
        isValid,
      }) => (
        <Form>
          <div className="row">
            <div className="form-group col-md-3 mt-3">
              <span className="has-float-label mb-3">
                <input
                  name="edrpo"
                  onChange={(e) => {
                    handleChange(e);
                    handleInputChange(e);
                  }}
                  onBlur={handleBlur}
                  value={values.edrpo}
                  className={`form-control ${
                    touched.edrpo && errors.edrpo ? "is-invalid" : ""
                  }`}
                  maxLength={15}
                  id="edrpo"
                  type="text"
                  placeholder="ЄДРПОУ"
                />
                {errors.edrpo ? (
                  <span className="text-danger">{errors.edrpo}</span>
                ) : (
                  <label htmlFor="surname">ЄДРПОУ</label>
                )}
              </span>
            </div>

            <div className="form-group col-md-6 mt-3">
              <span className="has-float-label mb-3">
                <input
                  name="cName"
                  onChange={(e) => {
                    handleChange(e);
                    handleInputChange(e);
                  }}
                  onBlur={handleBlur}
                  value={values.cName}
                  className={`form-control ${
                    touched.cName && errors.cName ? "is-invalid" : ""
                  }`}
                  maxLength={255}
                  id="cName"
                  type="text"
                  placeholder="Назва компанії"
                />
                {errors.cName ? (
                  <span className="text-danger">{errors.cName}</span>
                ) : (
                  <label htmlFor="surname">Назва компанії</label>
                )}
              </span>
            </div>
          </div>
          <div className="row">
            <div className="form-group col-md-6">
              <span className="has-float-label">
                <input
                  name="cAdress"
                  onChange={(e) => {
                    handleChange(e);
                    handleInputChange(e);
                  }}
                  onBlur={handleBlur}
                  value={values.cAdress}
                  className={`form-control ${
                    touched.cAdress && errors.cAdress ? "is-invalid" : ""
                  }`}
                  maxLength={255}
                  id="cAdress"
                  type="text"
                  placeholder="Адреса"
                />
                {errors.cAdress ? (
                  <span className="text-danger">{errors.cAdress}</span>
                ) : (
                  <label htmlFor="surname">Адреса</label>
                )}
              </span>
            </div>
            <div className="form-group col-md-3">
              <span className="has-float-label">
                <input
                  name="cPhone"
                  onChange={(e) => {
                    handleChange(e);
                    handleInputChange(e);
                  }}
                  onBlur={handleBlur}
                  value={values.cPhone}
                  className={`form-control ${
                    touched.cPhone && errors.cPhone ? "is-invalid" : ""
                  }`}
                  maxLength={13}
                  id="cPhone"
                  type="text"
                  placeholder="Телефон"
                />
                {errors.cPhone ? (
                  <span className="text-danger">{errors.cPhone}</span>
                ) : (
                  <label htmlFor="cPhone">Телефон</label>
                )}
              </span>
            </div>
          </div>

          <div className="row mt-3">
            <div className="form-group col-md-3 mb-3">
              <button
                disabled={!isValid}
                type="submit"
                className="btn custom-btn w-100"
              >
                Пошук
              </button>
            </div>
            <div className="form-group col-md-3">
              <button
                onClick={() => resetForm()}
                type="button"
                className="btn btn-danger w-100"
              >
                Очистити
              </button>
            </div>
          </div>
        </Form>
      )}
    </Formik>
  );
};

export default UrFormSearch;
