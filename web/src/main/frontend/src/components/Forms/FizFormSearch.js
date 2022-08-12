import React, { useEffect, useState } from "react";
import { Formik, Form, Field } from "formik";
import MaskedInput from "react-text-mask";
import * as Yup from "yup";
import Card from "../YPersonCard/Card.js";
import { useDispatch, useSelector } from "react-redux";
import PerPage from "../../common/PerPage";
import Spinner from "../../common/Loader";

import FloatInput from "../../common/FloatInput";
import { setSearchDataYPersonsThunk } from "../../store/reducers/FizSearchReducer";
import FormBtn from "../../common/FormBtn";
import {
  changeYPersonFormValueAC,
  resetFormValueAc,
  setCurrentPageCount,
} from "../../store/reducers/actions/YPersonActions";
import PaginationWithConfirm from "../../common/PaginationWithConfirm";

export const registerMask = [
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
  phone: Yup.string().matches("^(?:\\+)?(\\d{5,12})$", "Не вірний формат"),
  address: Yup.string().max(255, "Занадто довге"),
  inn: Yup.string().matches("^[0-9]{7,12}$", "7-12 Цифр"),
  passportNumber: Yup.string().matches("^[0-9]{6}$", "6 Цифр"),
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
  const [search, setSearch] = useState(false);
  const [, setPageSize] = useState(6);
  const [, setPageNo] = useState(0);
  const paginate = (pageNumber) => {
    dispatch(setCurrentPageCount(pageNumber - 1));
    setPageNo(pageNumber - 1);
  };
  const dispatch = useDispatch();
  const fizFormState = useSelector((state) => state.fiz);
  const [searchFormFiz, setSearchFormFiz] = useState({
    ...fizFormState.fizForm,
  });
  const handleInputChange = (e) => {
    setSearchFormFiz({ ...searchFormFiz, [e.target.name]: e.target.value });
  };
  const updateValFromStore = (key, val) => {
    dispatch(changeYPersonFormValueAC(key, val));
  };

  useEffect(() => {
    if (fizFormState.searchResults.length !== 0) {
      dispatch(
        setSearchDataYPersonsThunk(
          fizFormState.fizForm,
          fizFormState.currentPage,
          fizFormState.perPage
        )
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, fizFormState.perPage]);

  const handleInput = (e, key, handleChange) => {
    handleChange(e);
    handleInputChange(e);
    updateValFromStore(key, e.target.value);
  };
  const validate = (values) => {
    let errors = {};

    if (values.day === "" && values.month !== "") {
      errors.day = "Обов'зкове поле";
    }
    if (values.month === "" && values.day !== "") {
      errors.month = "Обов'зкове поле";
    }
    return errors;
  };
  return (
    <>
      <Formik
        initialValues={{
          ...fizFormState.fizForm,
        }}
        validationSchema={fizSchema}
        validate={validate}
        onSubmit={(values, { setSubmitting }) => {
          setSubmitting(false);
        }}
      >
        {({
          touched,
          errors,
          handleBlur,
          handleChange,
          resetForm,
          handleSubmit,
          isValid,
        }) => (
          <Form onSubmit={handleSubmit}>
            <div className="row mt-3">
              <FloatInput
                name={"surname"}
                label={"Прізвище"}
                val={fizFormState.fizForm.surname}
                errors={errors.surname}
                touched={touched.surname}
                max={64}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "surname", handleChange);
                }}
              />
              <FloatInput
                name={"name"}
                label={"Ім'я"}
                val={fizFormState.fizForm.name}
                errors={errors.name}
                touched={touched.name}
                max={64}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "name", handleChange);
                }}
              />
              <FloatInput
                name={"patronymic"}
                label={"По-батькові"}
                val={fizFormState.fizForm.patronymic}
                errors={errors.patronymic}
                touched={touched.patronymic}
                max={64}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "patronymic", handleChange);
                }}
              />
            </div>
            <div className="row mt-3">
              <FloatInput
                col={"col-md-3"}
                name={"day"}
                label={"День"}
                val={fizFormState.fizForm.day}
                errors={errors.day}
                touched={touched.day}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "day", handleChange);
                }}
                max={2}
              />
              <FloatInput
                col={"col-md-3"}
                name={"month"}
                label={"Місяць"}
                val={fizFormState.fizForm.month}
                errors={errors.month}
                touched={touched.month}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "month", handleChange);
                }}
                max={2}
              />
              <FloatInput
                col={"col-md-3"}
                name={"year"}
                label={"Рік"}
                val={fizFormState.fizForm.year}
                errors={errors.year}
                touched={touched.year}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "year", handleChange);
                }}
                max={4}
              />
              <FloatInput
                col={"col-md-3"}
                name={"age"}
                label={"Вік"}
                val={fizFormState.fizForm.age}
                errors={errors.age}
                touched={touched.age}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "age", handleChange);
                }}
                max={3}
              />
            </div>
            <div className="row mt-3">
              <FloatInput
                col={"col-md-5"}
                name={"phone"}
                label={"Телефон"}
                val={fizFormState.fizForm.phone}
                errors={errors.phone}
                touched={touched.phone}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "phone", handleChange);
                }}
                max={13}
              />
              <FloatInput
                col={"col-md-7"}
                name={"address"}
                label={"Адреса"}
                val={fizFormState.fizForm.address}
                errors={errors.address}
                touched={touched.address}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "address", handleChange);
                }}
                max={255}
              />
            </div>
            <div className="row mt-3">
              <div className="form-group col-md-3">
                <span className="has-float-label">
                  <select
                    name="documentType"
                    onChange={(e) => {
                      setDocumentType(e.target.value);
                      handleChange(e);
                      handleInputChange(e);
                      updateValFromStore("documentType", e.target.value);
                    }}
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
                  <FloatInput
                    col={"col-md-2"}
                    name={"passportSeria"}
                    label={"Серія"}
                    val={fizFormState.fizForm.passportSeria}
                    errors={errors.passportSeria}
                    touched={touched.passportSeria}
                    onBlur={handleBlur}
                    onChange={(e) => {
                      handleInput(e, "passportSeria", handleChange);
                    }}
                    max={2}
                  />
                  <FloatInput
                    col={"col-md-3"}
                    name={"passportNumber"}
                    label={"Номер Паспорта"}
                    val={fizFormState.fizForm.passportNumber}
                    errors={errors.passportNumber}
                    touched={touched.passportNumber}
                    onBlur={handleBlur}
                    onChange={(e) => {
                      handleInput(e, "passportNumber", handleChange);
                    }}
                    max={6}
                  />
                </>
              )}
              {documentType === "id_card" && (
                <>
                  <FloatInput
                    col={"col-md-2"}
                    name={"id_documentNumber"}
                    label={"Номер документа"}
                    val={fizFormState.fizForm.id_documentNumber}
                    errors={errors.id_documentNumber}
                    touched={touched.id_documentNumber}
                    onBlur={handleBlur}
                    onChange={(e) => {
                      handleInput(e, "id_documentNumber", handleChange);
                    }}
                    max={9}
                  />
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
                              updateValFromStore(
                                "id_registryNumber",
                                e.target.value
                              );
                            }}
                            onBlur={handleBlur}
                            value={fizFormState.fizForm.id_registryNumber}
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
                  <FloatInput
                    col={"col-md-2"}
                    name={"foreignP_documentNumber"}
                    label={"Номер документа"}
                    val={fizFormState.fizForm.foreignP_documentNumber}
                    errors={errors.foreignP_documentNumber}
                    touched={touched.foreignP_documentNumber}
                    onBlur={handleBlur}
                    onChange={(e) => {
                      handleInput(e, "foreignP_documentNumber", handleChange);
                    }}
                    max={8}
                  />
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
                              updateValFromStore(
                                "foreignP_registryNumber",
                                e.target.value
                              );
                            }}
                            onBlur={handleBlur}
                            value={fizFormState.fizForm.foreignP_registryNumber}
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
              <FloatInput
                name={"inn"}
                label={"ІПН"}
                val={fizFormState.fizForm.inn}
                errors={errors.inn}
                touched={touched.inn}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "inn", handleChange);
                }}
                max={12}
              />
              <FormBtn
                searchAction={() => {
                  setPageNo(0);
                  dispatch(
                    setSearchDataYPersonsThunk(
                      fizFormState.fizForm,
                      0,
                      fizFormState.perPage
                    )
                  );
                }}
                clearAction={() => {
                  dispatch(resetFormValueAc());
                  resetForm();
                }}
                disabled={!isValid}
              />
            </div>
          </Form>
        )}
      </Formik>
      {fizFormState.searchResults.length > 0 && (
        <PerPage
          pageSize={fizFormState.perPage}
          setPageNo={setPageNo}
          setPageSize={setPageSize}
          type={"fiz"}
        />
      )}
      <div className="d-flex flex-wrap">
        {fizFormState.searchResults?.map((el) => {
          return <Card key={el.id} data={el} />;
        })}
      </div>
      {fizFormState.searchResults.length > 0 && (
        <PaginationWithConfirm
          filesPerPage={fizFormState.perPage}
          totalFiles={fizFormState.totalElements}
          paginate={paginate}
          pageNo={fizFormState.currentPage}
          search={search}
          setSearch={setSearch}
        />
      )}
      <Spinner loader={fizFormState.loader} message={"Шукаю збіги"} />
    </>
  );
};

export default FizFormSearch;
