import React, { useState } from "react";
import { Formik, Form, Field } from "formik";
import MaskedInput from "react-text-mask";
import * as Yup from "yup";

import Card from "./Card";
import { setAlertMessageThunk } from "../store/reducers/AuthReducer";
import { useDispatch } from "react-redux";

const registerMask = [
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  "-",
  /\d/,
  /\d/,
  /\d/,
  /\d/,
  /\d/,
];

const fizSchema = Yup.object().shape({
  name: Yup.string().max(64, "Занадто довге"),
  surname: Yup.string().max(64, "Занадто довге"),
  patronymic: Yup.string().max(64, "Занадто довге"),
  day: Yup.string().matches("^([1-9]|[1-2][0-9]|[3][0-1])$", "1-31"),
  month: Yup.string().matches("^(1[0-2]|[1-9])$", "1-12"),
  year: Yup.string().matches("^(([1][9]|[2][0])\\d{2,2})|2100$", "1900-2100 "),
  age: Yup.string().matches("^([1-9]|[1-9][0-9]|1[01][0-9]|120)$", "1-120"),
  phone: Yup.string().matches("^(?:\\+38)?(\\d{10})$", "Не вірний формат"),
  address: Yup.string().max(255, "Занадто довге"),
  inn: Yup.string()
    .matches("^[0-9]{7,12}$", "7-12")
    .min(7, " Мін 7 Символів")
    .matches("^[0-9]{7,12}$", "Макс 12 Символів"),
  passportNumber: Yup.string()
    .min(6, "6 Символів")
    .matches("^[0-9]{6}$", "6 Цифр"),
  passportSeria: Yup.string().matches("^(?!0{2,2})[а-яА-Я]{2,2}$", "AA"),
  id_documentNumber: Yup.string().matches("^[0-9]{9}$", " 9 цифр"),
  id_registryNumber: Yup.string().matches(
    "^[0-9]{8}\\-[0-9]{5}$",
    "Формат 8цифр-5цифр"
  ),
  foreignP_documentNumber: Yup.string().matches(
    "^[a-z,A-Z]{2}[0-9]{6}$",
    "Формат AA123456"
  ),
  foreignP_registryNumber: Yup.string().matches(
    "^[0-9]{8}\\-[0-9]{5}$",
    "Формат 8 цифр-5 цифр"
  ),
});

const FizFormSearch = () => {
  const [documentType, setDocumentType] = useState("passport");
  const [searchResults, setSearchResults] = useState([]);
  const dispatch = useDispatch();
  const [searchFormFiz, setSearchFormFiz] = useState({
    name: "",
    surname: "",
    patronymic: "",
    day: "",
    month: "",
    year: "",
    age: "",
    phone: "",
    address: "",
    passportNumber: "",
    passportSeria: "",
    id_documentNumber: "",
    id_registryNumber: "",
    foreignP_documentNumber: "",
    foreignP_registryNumber: "",
    inn: "",
  });

  const handleInputChange = (e) => {
    setSearchFormFiz({ ...searchFormFiz, [e.target.name]: e.target.value });
  };
  const handleSubmitFizForm = (values) => {
    fetch("/api/uniPF/search", {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization:
          "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
      },
      method: "POST",
      body: JSON.stringify(values),
    })
      .then((res) => res.json())
      .then((file) => {
        if (file.length > 0) {
          setSearchResults(file);
        } else {
          dispatch(
            setAlertMessageThunk(
              "За данними пошуку збігів не знайдено",
              "danger"
            )
          );
        }
      })

      .catch(function (res) {
        console.log(res);
      });
  };

  return (
    <>
      <Formik
        initialValues={{
          name: "",
          surname: "",
          patronymic: "",
          day: "",
          month: "",
          year: "",
          age: "",
          phone: "",
          address: "",
          passportNumber: "",
          passportSeria: "",
          id_documentNumber: "",
          id_registryNumber: "",
          foreignP_documentNumber: "",
          foreignP_registryNumber: "",
          inn: "",
        }}
        validationSchema={fizSchema}
        onSubmit={(values, { setSubmitting }) => {
          setSubmitting(false);
          handleSubmitFizForm(values);
        }}
      >
        {({
          touched,
          errors,

          handleBlur,
          handleChange,
          resetForm,
          handleSubmit,
          values,
          isValid,
        }) => (
          <Form onSubmit={handleSubmit}>
            <div className="row mt-3">
              <div className="form-group col-md-4">
                <span className="has-float-label">
                  <input
                    name="surname"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.surname}
                    className={`form-control ${
                      touched.surname && errors.surname ? "is-invalid" : ""
                    }`}
                    maxLength={64}
                    id="surname"
                    type="text"
                    placeholder="Прізвище"
                  />
                  {errors.surname ? (
                    <span className="text-danger">{errors.surname}</span>
                  ) : (
                    <label htmlFor="surname">Прізвище</label>
                  )}
                </span>
              </div>
              <div className="form-group col-md-4">
                <span className="has-float-label">
                  <input
                    name="name"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.name}
                    className={`form-control ${
                      touched.name && errors.name ? "is-invalid" : ""
                    }`}
                    maxLength={64}
                    id="name"
                    type="text"
                    placeholder="Ім'я"
                  />
                  {errors.name ? (
                    <span className="text-danger">{errors.name}</span>
                  ) : (
                    <label htmlFor="name">Ім'я</label>
                  )}
                </span>
              </div>
              <div className="form-group col-md-4">
                <span className="has-float-label">
                  <input
                    name="patronymic"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.patronymic}
                    className={`form-control ${
                      touched.patronymic && errors.patronymic
                        ? "is-invalid"
                        : ""
                    }`}
                    maxLength={64}
                    id="patronymic"
                    type="text"
                    placeholder="По-батькові"
                  />
                  {errors.surname ? (
                    <span className="text-danger">{errors.patronymic}</span>
                  ) : (
                    <label htmlFor="patronymic">По-батькові</label>
                  )}
                </span>
              </div>
            </div>
            <div className="row mt-3">
              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <input
                    name="day"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.day}
                    className={`form-control ${
                      touched.day && errors.day ? "is-invalid" : ""
                    }`}
                    id="day"
                    type="text"
                    placeholder="День"
                    maxLength={2}
                    max={31}
                    min={1}
                  />
                  {errors.day ? (
                    <span className="text-danger">{errors.day}</span>
                  ) : (
                    <label htmlFor="day">День</label>
                  )}
                </span>
              </div>

              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <input
                    name="month"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.month}
                    className={`form-control ${
                      touched.month && errors.month ? "is-invalid" : ""
                    }`}
                    maxLength={2}
                    id="month"
                    type="text"
                    placeholder="Місяць"
                  />
                  {errors.month ? (
                    <span className="text-danger">{errors.month}</span>
                  ) : (
                    <label htmlFor="month">Місяць</label>
                  )}
                </span>
              </div>
              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <input
                    name="year"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.year}
                    className={`form-control ${
                      touched.year && errors.year ? "is-invalid" : ""
                    }`}
                    maxLength={4}
                    id="year"
                    type="text"
                    placeholder="Рік"
                  />
                  {errors.year ? (
                    <span className="small text-danger">{errors.year}</span>
                  ) : (
                    <label htmlFor="year">Рік</label>
                  )}
                </span>
              </div>
              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <input
                    name="age"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.age}
                    className={`form-control ${
                      touched.age && errors.age ? "is-invalid" : ""
                    }`}
                    maxLength={3}
                    id="age"
                    type="string"
                    placeholder="Вік"
                  />
                  {errors.age ? (
                    <span className="text-danger">{errors.age}</span>
                  ) : (
                    <label htmlFor="age">Вік</label>
                  )}
                </span>
              </div>
            </div>
            <div className="row mt-3">
              <div className="form-group col-md-5">
                <span className="has-float-label">
                  <input
                    name="phone"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.phone}
                    className={`form-control ${
                      touched.phone && errors.phone ? "is-invalid" : ""
                    }`}
                    maxLength={13}
                    id="phone"
                    type="text"
                    placeholder="Телефон"
                  />
                  {errors.phone ? (
                    <span className="text-danger">{errors.phone}</span>
                  ) : (
                    <label htmlFor="phone">Телефон</label>
                  )}
                </span>
              </div>
              <div className="form-group col-md-7">
                <span className="has-float-label">
                  <input
                    name="address"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.address}
                    className={`form-control ${
                      touched.address && errors.address ? "is-invalid" : ""
                    }`}
                    maxLength={255}
                    id="address"
                    type="text"
                    placeholder="Адреса"
                  />
                  {errors.address ? (
                    <span className="text-danger">{errors.address}</span>
                  ) : (
                    <label htmlFor="address">Адреса</label>
                  )}
                </span>
              </div>
            </div>
            <div className="row mt-3">
              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <select
                    name="documentType"
                    onChange={(e) => setDocumentType(e.target.value)}
                    value={documentType}
                    className="form-select"
                  >
                    <option value="passport">Паспорт</option>
                    <option value="id_card">ID картка</option>
                    <option value="foreign_passport">
                      Закордонний паспорт
                    </option>
                  </select>
                </span>
              </div>

              {documentType === "passport" && (
                <>
                  <div className="form-group col-md-2">
                    <span className="has-float-label">
                      <input
                        name="passportSeria"
                        onChange={(e) => {
                          handleChange(e);
                          handleInputChange(e);
                        }}
                        onBlur={handleBlur}
                        value={values.passportSeria}
                        className={`form-control ${
                          touched.passportSeria && errors.passportSeria
                            ? "is-invalid"
                            : ""
                        }`}
                        maxLength={2}
                        id="passportSeria"
                        type="text"
                        placeholder="Серія"
                      />
                      {errors.passportSeria ? (
                        <span className="text-danger">
                          {errors.passportSeria}
                        </span>
                      ) : (
                        <label htmlFor="passportSeria">Серія</label>
                      )}
                    </span>
                  </div>
                  <div className="form-group col-md-3">
                    <span className="has-float-label">
                      <input
                        name="passportNumber"
                        onChange={(e) => {
                          handleChange(e);
                          handleInputChange(e);
                        }}
                        onBlur={handleBlur}
                        value={values.passportNumber}
                        className={`form-control ${
                          touched.passportNumber && errors.passportNumber
                            ? "is-invalid"
                            : ""
                        }`}
                        maxLength={6}
                        id="passportNumber"
                        type="text"
                        placeholder="Номер Паспорта"
                      />
                      {errors.passportNumber ? (
                        <span className="text-danger">
                          {errors.passportNumber}
                        </span>
                      ) : (
                        <label htmlFor="passportNumber">Номер</label>
                      )}
                    </span>
                  </div>
                </>
              )}
              {documentType === "id_card" && (
                <>
                  <div className="form-group col-md-2">
                    <span className="has-float-label">
                      <input
                        name="id_documentNumber"
                        onChange={(e) => {
                          handleChange(e);
                          handleInputChange(e);
                        }}
                        onBlur={handleBlur}
                        value={values.id_documentNumber}
                        className={`form-control ${
                          touched.id_documentNumber && errors.id_documentNumber
                            ? "is-invalid"
                            : ""
                        }`}
                        maxLength={9}
                        id="id_documentNumber"
                        type="text"
                        placeholder="Номер документа"
                      />
                      {errors.id_documentNumber ? (
                        <span className="text-danger">
                          {errors.id_documentNumber}
                        </span>
                      ) : (
                        <label htmlFor="id_documentNumber">№ документа</label>
                      )}
                    </span>
                  </div>
                  <div className="form-group col-md-3">
                    <span className="has-float-label">
                      <Field>
                        {({ field }) => (
                          <MaskedInput
                            {...field}
                            mask={registerMask}
                            name="id_registryNumber"
                            onChange={(e) => {
                              handleChange(e);
                              handleInputChange(e);
                            }}
                            onBlur={handleBlur}
                            value={values.id_registryNumber}
                            className={`form-control ${
                              touched.id_registryNumber &&
                              errors.id_registryNumber
                                ? "is-invalid"
                                : ""
                            }`}
                            id="id_registryNumber"
                            type="text"
                            placeholder="Номер запису в реєстрі"
                          />
                        )}
                      </Field>

                      {errors.id_registryNumber ? (
                        <span className="text-danger">
                          {errors.id_registryNumber}
                        </span>
                      ) : (
                        <label htmlFor="id_registryNumber">№ в реєстрі</label>
                      )}
                    </span>
                  </div>
                </>
              )}
              {documentType === "foreign_passport" && (
                <>
                  <div className="form-group col-md-2">
                    <span className="has-float-label">
                      <input
                        name="foreignP_documentNumber"
                        onChange={(e) => {
                          handleChange(e);
                          handleInputChange(e);
                        }}
                        onBlur={handleBlur}
                        value={values.foreignP_documentNumber}
                        className={`form-control ${
                          touched.foreignP_documentNumber &&
                          errors.foreignP_documentNumber
                            ? "is-invalid"
                            : ""
                        }`}
                        maxLength={8}
                        id="foreignP_documentNumber"
                        type="text"
                        placeholder="Номер документа"
                      />
                      {errors.foreignP_documentNumber ? (
                        <span className="text-danger">
                          {errors.foreignP_documentNumber}
                        </span>
                      ) : (
                        <label htmlFor="foreignP_documentNumber">
                          № документа
                        </label>
                      )}
                    </span>
                  </div>
                  <div className="form-group col-md-3">
                    <span className="has-float-label">
                      <Field>
                        {({ field }) => (
                          <MaskedInput
                            {...field}
                            mask={registerMask}
                            name="foreignP_registryNumber"
                            onChange={(e) => {
                              handleChange(e);
                              handleInputChange(e);
                            }}
                            onBlur={handleBlur}
                            value={values.foreignP_registryNumber}
                            className={`form-control ${
                              touched.foreignP_registryNumber &&
                              errors.foreignP_registryNumber
                                ? "is-invalid"
                                : ""
                            }`}
                            id="foreignP_registryNumber"
                            type="text"
                            placeholder="Номер запису в реестрі"
                          />
                        )}
                      </Field>
                      {errors.foreignP_registryNumber ? (
                        <span className="text-danger">
                          {errors.foreignP_registryNumber}
                        </span>
                      ) : (
                        <label htmlFor="foreignP_registryNumber">
                          № в реєстрі
                        </label>
                      )}
                    </span>
                  </div>
                </>
              )}

              <div className="form-group col-md-4">
                <span className="has-float-label mb-3">
                  <input
                    name="inn"
                    onChange={(e) => {
                      handleChange(e);
                      handleInputChange(e);
                    }}
                    onBlur={handleBlur}
                    value={values.inn}
                    className={`form-control ${
                      touched.inn && errors.inn ? "is-invalid" : ""
                    }`}
                    maxLength={12}
                    id="inn"
                    type="text"
                    placeholder="IНН"
                  />
                  {errors.inn ? (
                    <span className="text-danger">{errors.inn}</span>
                  ) : (
                    <label htmlFor="inn">ІПН</label>
                  )}
                </span>
              </div>
              <div className="row mt-3">
                <div className="form-group col-md-3 mb-3">
                  <button
                    disabled={!isValid}
                    type="submitt"
                    className="btn btn-success w-100"
                  >
                    Пошук
                  </button>
                </div>
                <div className="form-group col-md-3">
                  <button
                    onClick={() => resetForm()}
                    type="button"
                    className="btn btn-primary w-100"
                  >
                    Очистити
                  </button>
                </div>
              </div>
            </div>
          </Form>
        )}
      </Formik>
      <div className="d-flex flex-wrap justify-content-center">
        {searchResults?.map((el) => {
          return <Card data={el} />;
        })}
      </div>
    </>
  );
};

export default FizFormSearch;
