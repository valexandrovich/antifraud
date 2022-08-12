import React, { useState } from "react";
import { DateObject } from "react-multi-date-picker";
import { pad, sourceName } from "./Card";
import * as IoIcons from "react-icons/io";
import * as Yup from "yup";
import { Field, Form, Formik } from "formik";
import MaskedInput from "react-text-mask";
import { registerMask } from "../Forms/FizFormSearch";
import { date } from "yup";
import { useDispatch, useSelector } from "react-redux";
import passportService from "../../api/YPassportApi";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";
import ConfirmDeletemodal from "../Modal/ConfirmDeletemodal";

const today = new Date();
const minDate = new Date("1991");

const uaIdCard = Yup.object().shape({
  number: Yup.string()
    .nullable()
    .matches("^[0-9]{9}$", "Невірний формат 9 цифр"),
  recordNumber: Yup.string()
    .nullable()
    .matches("^[0-9]{8}\\-[0-9]{5}$", "Невірний формат 8цифр-5цифр"),
  authority: Yup.string().nullable().max(250, "Занадто довге"),
  issued: date()
    .nullable()
    .max(today, "Невірний формат дата мае бути менше за теперішню")
    .min(minDate, "Невірний формат дата мае бути більше 1991 року"),
});

const foreignPassport = Yup.object().shape({
  number: Yup.string()
    .nullable()
    .matches("^[0-9]{6}$", "Невірний формат 6 цифр"),
  series: Yup.string().nullable().matches("^[a-z,A-Z]{2}$", "Невірний формат"),
  recordNumber: Yup.string()
    .nullable()
    .matches("^[0-9]{8}\\-[0-9]{5}$", "Невірний формат 8цифр-5цифр"),
  authority: Yup.string().nullable().max(250, "Занадто довге"),
  issued: date()
    .nullable()
    .max(today, "Невірний формат дата мае бути менше за теперішню")
    .min(minDate, "Невірний формат дата мае бути більше 1991 року"),
});

const uaPassport = Yup.object().shape({
  number: Yup.string()
    .nullable()
    .matches("^[0-9]{6}$", "Невірний формат 6 цифр"),
  series: Yup.string().nullable().matches("^[а-я,А-Я]{2}$", "Невірний формат"),
  authority: Yup.string().nullable().max(250, "Занадто довге"),
  issued: date()
    .nullable()
    .max(today, "Невірний формат дата мае бути менше за теперішню")
    .min(minDate, "Невірний формат дата мае бути більше 1991 року"),
});
const Passports = ({ data, onChange, onClick }) => {
  const {
    id,
    series,
    number,
    authority,
    issued,
    endDate,
    recordNumber,
    type,
    validity,
    importSources,
  } = data;
  const dispatch = useDispatch();
  const [edit, setEdit] = useState(true);
  const [confirmationRemove, setConfirmationRemove] = useState(null);
  const userRole = useSelector((state) => state.auth.role);
  const update = (values) => {
    passportService.updatePassport(values).then((res) => {
      if (res.status > 300) {
        dispatch(
          setAlertMessageThunk(
            res.data?.messages?.map((m) => m),
            "danger"
          )
        );
        setEdit(false);
      }
      if (res.status === 200) {
        setEdit(!edit);
      }
    });
  };

  const editpassportType = () => {
    if (type === "UA_IDCARD") {
      return (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  number: pad(number, 9),
                  recordNumber: recordNumber ? recordNumber : "",
                  authority: authority ? authority : "",
                  issued: issued ? issued : "",
                  endDate: endDate ? endDate : "",
                  validity: Boolean(validity),
                  type,
                  id,
                }}
                validationSchema={uaIdCard}
                onSubmit={(values) => {
                  update(values);
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
                        onClick={() => setConfirmationRemove(values.id)}
                        className="pointer"
                        style={{ fontSize: "24px", color: "red" }}
                      />
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="email">№ паспорту:</label>
                      <input
                        name="number"
                        maxLength={9}
                        id={id}
                        className={`form-control ${
                          touched.number && errors.number ? "is-invalid" : ""
                        }`}
                        value={values.number}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.number ? (
                        <span className="text-danger">{errors.number}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="email">№ запису:</label>
                      <Field>
                        {({ field }) => (
                          <MaskedInput
                            {...field}
                            mask={registerMask}
                            name="recordNumber"
                            onChange={(e) => {
                              onChange(e);
                              handleChange(e);
                            }}
                            onBlur={handleBlur}
                            value={values.recordNumber}
                            className={`form-control ${
                              touched.recordNumber && errors.recordNumber
                                ? "is-invalid"
                                : ""
                            }`}
                            id={id}
                            type="text"
                          />
                        )}
                      </Field>
                      {errors.recordNumber ? (
                        <span className="text-danger">
                          {errors.recordNumber}
                        </span>
                      ) : null}
                    </div>
                    <div className="col-sm-6 mr-10">
                      <label htmlFor="email">Ким виданий:</label>
                      <input
                        name="authority"
                        maxLength={255}
                        id={id}
                        className={`form-control ${
                          touched.authority && errors.authority
                            ? "is-invalid"
                            : ""
                        }`}
                        value={values.authority}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.authority ? (
                        <span className="text-danger">{errors.authority}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="issued">Дата видачі:</label>
                      <input
                        name="issued"
                        id={id}
                        min={"1991-08-24"}
                        max={new Date().toISOString().slice(0, -14)}
                        className={`form-control ${
                          touched.issued && errors.issued ? "is-invalid" : ""
                        }`}
                        value={values.issued}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                      {errors.issued ? (
                        <span className="text-danger">{errors.issued}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="endDate">Дійсний до:</label>
                      <input
                        name="endDate"
                        id={id}
                        className="form-control"
                        value={values.endDate}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                    </div>
                    <div className="d-flex align-items-center mt-2">
                      <label htmlFor="validity">Статус:</label>
                      <input
                        id={id}
                        name="validity"
                        className="big-checkbox ml-10"
                        checked={validity}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        type="checkbox"
                      />
                      <span>{validity ? "Дійсний" : "Недійсний"}</span>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        </div>
      );
    }
    if (type === "UA_FOREIGN") {
      return (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  number: pad(number, 6),
                  series: series ? series : "",
                  recordNumber: recordNumber ? recordNumber : "",
                  authority: authority ? authority : "",
                  issued: issued ? issued : "",
                  endDate: endDate ? endDate : "",
                  validity: Boolean(validity),
                  type,
                  id,
                }}
                validationSchema={foreignPassport}
                onSubmit={(values) => {
                  setEdit(!edit);
                  update(values);
                  console.log(values, "foreignPassport");
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
                        onClick={() => setConfirmationRemove(values.id)}
                        className="pointer"
                        style={{ fontSize: "24px", color: "red" }}
                      />
                    </div>
                    <div className="d-flex">
                      <div className="col-sm-2 mr-10">
                        <label htmlFor="series">Серія:</label>
                        <input
                          id={id}
                          maxLength={2}
                          name="series"
                          className={`form-control ${
                            touched.series && errors.series ? "is-invalid" : ""
                          }`}
                          value={values.series}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="text"
                        />
                        {errors.series ? (
                          <span className="text-danger">{errors.series}</span>
                        ) : null}
                      </div>
                      <div className="col-sm-3 mr-10">
                        <label htmlFor="email">№ паспорту:</label>
                        <input
                          name="number"
                          maxLength={6}
                          id={id}
                          className={`form-control ${
                            touched.number && errors.number ? "is-invalid" : ""
                          }`}
                          value={values.number}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="text"
                        />
                        {errors.number ? (
                          <span className="text-danger">{errors.number}</span>
                        ) : null}
                      </div>
                    </div>
                    <div className="col-sm-3 mr-10">
                      <label htmlFor="email">№ запису:</label>
                      <Field>
                        {({ field }) => (
                          <MaskedInput
                            {...field}
                            mask={registerMask}
                            name="recordNumber"
                            onChange={(e) => {
                              onChange(e);
                              handleChange(e);
                            }}
                            onBlur={handleBlur}
                            value={values.recordNumber}
                            className={`form-control ${
                              touched.recordNumber && errors.recordNumber
                                ? "is-invalid"
                                : ""
                            }`}
                            id={id}
                            type="text"
                          />
                        )}
                      </Field>
                      {errors.recordNumber ? (
                        <span className="text-danger">
                          {errors.recordNumber}
                        </span>
                      ) : null}
                    </div>
                    <div className="col-sm-6 mr-10">
                      <label htmlFor="email">Ким виданий:</label>
                      <input
                        name="authority"
                        maxLength={255}
                        id={id}
                        className={`form-control ${
                          touched.authority && errors.authority
                            ? "is-invalid"
                            : ""
                        }`}
                        value={values.authority}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.authority ? (
                        <span className="text-danger">{errors.authority}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="issued">Дата видачі:</label>
                      <input
                        name="issued"
                        id={id}
                        min={"1991-08-24"}
                        max={new Date().toISOString().slice(0, -14)}
                        className={`form-control ${
                          touched.issued && errors.issued ? "is-invalid" : ""
                        }`}
                        value={values.issued}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                      {errors.issued ? (
                        <span className="text-danger">{errors.issued}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="endDate">Дійсний до:</label>
                      <input
                        name="endDate"
                        id={id}
                        className="form-control"
                        value={values.endDate}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                    </div>
                    <div className="d-flex align-items-center mt-2">
                      <label htmlFor="validity">Статус:</label>
                      <input
                        id={id}
                        name="validity"
                        className="big-checkbox ml-10"
                        checked={validity}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        type="checkbox"
                      />
                      <span>{validity ? "Дійсний" : "Недійсний"}</span>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        </div>
      );
    }
    if (type === "UA_DOMESTIC") {
      return (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  number: pad(number, 6),
                  series: series ? series : "",
                  authority: authority ? authority : "",
                  issued: issued ? issued : "",
                  endDate: endDate ? endDate : "",
                  validity: Boolean(validity),
                  type,
                  id,
                }}
                validationSchema={uaPassport}
                onSubmit={(values) => {
                  setEdit(!edit);
                  update(values);
                  console.log(values, "uaPassport");
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
                        onClick={() => setConfirmationRemove(values.id)}
                        className="pointer"
                        style={{ fontSize: "24px", color: "red" }}
                      />
                    </div>

                    <div className="d-flex">
                      <div className="col-sm-2 mr-10">
                        <label htmlFor="series">Серія:</label>
                        <input
                          id={id}
                          maxLength={2}
                          name="series"
                          className={`form-control ${
                            touched.series && errors.series ? "is-invalid" : ""
                          }`}
                          value={values.series}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="text"
                        />
                        {errors.series ? (
                          <span className="text-danger">{errors.series}</span>
                        ) : null}
                      </div>
                      <div className="col-sm-3 mr-10">
                        <label htmlFor="email">№ паспорту:</label>
                        <input
                          name="number"
                          maxLength={6}
                          id={id}
                          className={`form-control ${
                            touched.number && errors.number ? "is-invalid" : ""
                          }`}
                          value={values.number}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="text"
                        />
                        {errors.number ? (
                          <span className="text-danger">{errors.number}</span>
                        ) : null}
                      </div>
                    </div>

                    <div className="col-sm-6 mr-10">
                      <label htmlFor="email">Ким виданий:</label>
                      <input
                        name="authority"
                        maxLength={255}
                        id={id}
                        className={`form-control ${
                          touched.authority && errors.authority
                            ? "is-invalid"
                            : ""
                        }`}
                        value={values.authority}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="text"
                      />
                      {errors.authority ? (
                        <span className="text-danger">{errors.authority}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="issued">Дата видачі:</label>
                      <input
                        name="issued"
                        id={id}
                        min={"1991-08-24"}
                        max={new Date().toISOString().slice(0, -14)}
                        className={`form-control ${
                          touched.issued && errors.issued ? "is-invalid" : ""
                        }`}
                        value={values.issued}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                      {errors.issued ? (
                        <span className="text-danger">{errors.issued}</span>
                      ) : null}
                    </div>
                    <div className="col-sm-2 mr-10">
                      <label htmlFor="endDate">Дійсний до:</label>
                      <input
                        name="endDate"
                        id={id}
                        className="form-control"
                        value={values.endDate}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                        type="date"
                      />
                    </div>
                    <div className="d-flex align-items-center mt-2">
                      <label htmlFor="validity">Статус:</label>
                      <input
                        id={id}
                        name="validity"
                        className="big-checkbox ml-10"
                        checked={validity}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        type="checkbox"
                      />
                      <span>{validity ? "Дійсний" : "Недійсний"}</span>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </div>
        </div>
      );
    }
  };
  const passpordType = () => {
    if (type === "UA_IDCARD") {
      return (
        <>
          <p className={"ml-10"}>
            <b className="mr-10">ID картка:</b>
            {pad(number, 9)}
            <span className="ml-10">
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
          </p>
          <p className={"ml-10"}>
            <b className="mr-10">Запис №:</b>
            {recordNumber}
          </p>
        </>
      );
    }
    if (type === "UA_FOREIGN") {
      return (
        <>
          <p className={"ml-10"}>
            <b className="mr-10">Закордонний паспорт:</b>
            {series}
            {pad(number, 6)}
            <span className="ml-10">
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
          </p>
          <p className={"ml-10"}>
            <b className="mr-10">Запис №:</b>
            {recordNumber}
          </p>
        </>
      );
    }
    if (type === "UA_DOMESTIC") {
      return (
        <p className={"ml-10"}>
          <b className="mr-10">Паспорт:</b>
          {series}
          {pad(number, 6)}
          <span className="ml-10">
            {importSources && importSources.length > 0
              ? `(${importSources.length} ${sourceName(importSources)})`
              : ""}
          </span>
        </p>
      );
    }
  };
  return (
    <>
      {edit ? (
        <div>
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
            <div>
              {passpordType()}
              <p className={"ml-10"}>
                <b className="mr-10">Ким виданий:</b>
                {!authority ? "(не вказано)" : authority}
              </p>
              <p className={"ml-10"}>
                <b className="mr-10">Дата видачі:</b>
                {!issued
                  ? "(невідомо)"
                  : new DateObject(issued).format("DD.MM.YYYY")}
              </p>
              <p className={"ml-10"}>
                <b className="mr-10">Дійсний до:</b>
                {!endDate
                  ? "(не вказано)"
                  : new DateObject(endDate).format("DD.MM.YYYY")}
              </p>
              <p className={"ml-10"}>
                <b className="mr-10">Статус:</b>
                {validity === true ? "Дійсний" : "Недійсний"}
              </p>
            </div>
          </div>
        </div>
      ) : (
        <div>
          <div>{editpassportType()}</div>
        </div>
      )}
      {confirmationRemove && (
        <ConfirmDeletemodal
          open
          onClose={() => setConfirmationRemove(null)}
          uuid={confirmationRemove}
          deleteAction={onClick}
        />
      )}
    </>
  );
};

export default Passports;
