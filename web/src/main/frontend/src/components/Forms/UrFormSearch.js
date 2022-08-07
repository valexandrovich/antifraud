import React, { useEffect, useState } from "react";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import FloatInput from "../../common/FloatInput";
import { useDispatch, useSelector } from "react-redux";
import { setSearchYCompaniesDataThunk } from "../../store/reducers/UrSearchReducer";
import Spinner from "../../common/Loader";
import FormBtn from "../../common/FormBtn";
import {
  changeYCompanyFormValueAC,
  resetYCompanyFormValueAc,
  setCurrentPageYcompanyCount,
} from "../../store/reducers/actions/YcompanyActions";
import Card from "../YCompanyCard/Card";
import PerPage from "../../common/PerPage";
import Pagination from "../../common/Pagination";

const urSchema = Yup.object().shape({
  edrpou: Yup.string().matches("^[0-9]{8,9}$", "8-9 Цифр"),
  name: Yup.string().max(255, "Занадто довге"),
  address: Yup.string().max(255, "Занадто довге"),
  pdv: Yup.string().matches("^[0-9]{12}$", "12 Цифр"),
});

const UrFormSearch = () => {
  const dispatch = useDispatch();
  const urFormState = useSelector((state) => state.ur);
  const [, setPageSize] = useState(6);
  const [, setPageNo] = useState(0);
  const paginate = (pageNumber) => {
    dispatch(setCurrentPageYcompanyCount(pageNumber - 1));
    setPageNo(pageNumber - 1);
  };
  const [searchFormUr, setSearchFormUr] = useState({
    ...urFormState.urForm,
  });

  const handleInputChange = (e) => {
    setSearchFormUr({ ...searchFormUr, [e.target.name]: e.target.value });
  };
  const updateValFromStore = (key, val) => {
    dispatch(changeYCompanyFormValueAC(key, val));
  };
  const handleInput = (e, key, handleChange) => {
    handleChange(e);
    handleInputChange(e);
    updateValFromStore(key, e.target.value);
  };
  useEffect(() => {
    if (urFormState.searchResults.length !== 0) {
      dispatch(
        setSearchYCompaniesDataThunk(
          urFormState.urForm,
          urFormState.currentPage,
          urFormState.perPage
        )
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [urFormState.currentPage, urFormState.perPage]);

  return (
    <>
      <Formik
        initialValues={{ ...urFormState.urForm }}
        validationSchema={urSchema}
        onSubmit={(values, { setSubmitting }) => {
          setSubmitting(false);
        }}
      >
        {({
          touched,
          errors,
          resetForm,
          handleBlur,
          handleChange,
          isValid,
        }) => (
          <Form>
            <div className="row">
              <FloatInput
                col={"col-md-6 mt-3"}
                name={"name"}
                label={"Ім'я"}
                val={urFormState.urForm.name}
                errors={errors.name}
                touched={touched.name}
                max={255}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "name", handleChange);
                }}
              />
              <FloatInput
                col={"col-md-3 mt-3"}
                name={"edrpou"}
                label={"ЄДРПОУ"}
                val={urFormState.urForm.edrpou}
                errors={errors.edrpou}
                touched={touched.edrpou}
                max={9}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "edrpou", handleChange);
                }}
              />
            </div>
            <div className="row">
              <FloatInput
                col={"col-md-6 mt-3"}
                name={"address"}
                label={"Адреса"}
                val={urFormState.urForm.address}
                errors={errors.address}
                touched={touched.address}
                max={255}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "address", handleChange);
                }}
              />
              <FloatInput
                col={"col-md-3 mt-3"}
                name={"pdv"}
                label={"Код платника ПДВ"}
                val={urFormState.urForm.pdv}
                errors={errors.pdv}
                touched={touched.pdv}
                max={12}
                onBlur={handleBlur}
                onChange={(e) => {
                  handleInput(e, "pdv", handleChange);
                }}
              />
            </div>
            <FormBtn
              searchAction={() => {
                setPageNo(0);
                dispatch(
                  setSearchYCompaniesDataThunk(
                    urFormState.urForm,
                    0,
                    urFormState.perPage
                  )
                );
              }}
              disabled={!isValid}
              clearAction={() => {
                dispatch(resetYCompanyFormValueAc());
                resetForm();
              }}
            />
          </Form>
        )}
      </Formik>
      {urFormState.searchResults.length > 0 && (
        <PerPage
          pageSize={urFormState.perPage}
          setPageNo={setPageNo}
          setPageSize={setPageSize}
          type={"ur"}
        />
      )}

      <div className="d-flex flex-wrap">
        {urFormState.searchResults?.map((el) => {
          return <Card key={el.id} data={el} />;
        })}
      </div>
      {urFormState.searchResults.length > 0 && (
        <Pagination
          filesPerPage={urFormState.perPage}
          totalFiles={urFormState.totalElements}
          paginate={paginate}
          pageNo={urFormState.currentPage}
        />
      )}
      <Spinner loader={urFormState.loader} message={"Шукаю збіги"} />
    </>
  );
};

export default UrFormSearch;
