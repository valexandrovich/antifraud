import React from "react";
import { useDispatch } from "react-redux";
import {
  changeFormValueAC,
  submitUserAuthThunk,
} from "../../store/reducers/AuthReducer";
import * as yup from "yup";
import { Formik, Form } from "formik";

export const schema = yup.object().shape({
  login: yup.string().required("Введiть логін"),
  password: yup.string().required("Введiть пароль"),
});

const Login = () => {
  const dispatch = useDispatch();
  const updateValFromStore = (key, val) => {
    dispatch(changeFormValueAC(key, val));
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center align-items-center">
        <div id="login-column" className="col-md-6">
          <div id="login-box" className="col-md-12">
            <h3 className="login-box-msg">Авторизуйтесь для початку роботи</h3>
            <Formik
              initialValues={{
                login: "",
                password: "",
              }}
              validationSchema={schema}
              onSubmit={(values, { setSubmitting }) => {
                dispatch(submitUserAuthThunk(values));

                setSubmitting(false);
              }}
            >
              {({
                values,
                touched,
                errors,
                handleChange,
                handleBlur,
                handleSubmit,
                isValid,
              }) => {
                return (
                  <Form onSubmit={handleSubmit}>
                    <div className="input-group mb-3">
                      <input
                        name="login"
                        onChange={(e) => {
                          handleChange(e);
                          updateValFromStore("login", e.target.value);
                        }}
                        onBlur={handleBlur}
                        value={values.login}
                        type="text"
                        className="form-control"
                        placeholder="Логін"
                      />
                    </div>
                    <div
                      className={
                        errors.login
                          ? "input-feedback text-danger error-margin"
                          : "input-feedback"
                      }
                    >
                      {touched.login ? errors.login : ""}
                    </div>
                    <div className="input-group mb-3">
                      <input
                        name="password"
                        onChange={(e) => {
                          handleChange(e);
                          updateValFromStore("password", e.target.value);
                        }}
                        onBlur={handleBlur}
                        value={values.password}
                        type="password"
                        className="form-control"
                        placeholder="Пароль"
                      />
                    </div>

                    <div
                      className={
                        errors.password
                          ? "input-feedback text-danger error-margin"
                          : "input-feedback"
                      }
                    >
                      {touched.password ? errors.password : ""}
                    </div>
                    <div className="row">
                      <div className="col-2 mx-auto">
                        <button
                          disabled={!isValid}
                          onClick={handleSubmit}
                          type="submit"
                          className="btn btn-success p-2"
                        >
                          Вхід
                        </button>
                      </div>
                    </div>
                  </Form>
                );
              }}
            </Formik>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
