import React, { useState } from "react";
import { DateObject } from "react-multi-date-picker";
import { sourceName } from "./Card";
import { useSelector } from "react-redux";
import * as IoIcons from "react-icons/io";
import { Form, Formik } from "formik";
import * as Yup from "yup";

const tagOptions = [
  { code: "RP", description: "Отношение к банку / Партнер" },
  { code: "REP", description: "Отношение к банку / Сотрудник / Практикант" },
  { code: "RES", description: "Отношение к банку / Сотрудник / Штатный" },
  { code: "RER", description: "Отношение к банку / Сотрудник / Родственник" },
  {
    code: "RED",
    description: "Отношение к банку / Сотрудник / Договор подряда",
  },
  { code: "RC", description: "Отношение к банку / Клиент / Договор подряда" },
  { code: "NAT", description: "Негатив / Блокировка / Террорист" },
  { code: "NAF", description: "Негатив / Блокировка / Мошенник" },
  {
    code: "NAL",
    description: "Негатив / Блокировка / Недействительный паспорт (книжка)",
  },
  {
    code: "NBC1",
    description: "Негатив / Черный список / Криминал / Умышленные преступления",
  },
  {
    code: "NBA1",
    description:
      "Негатив / Черный список / Административная ответственность / Уровень 1",
  },
  {
    code: "NBA2",
    description:
      "Негатив / Черный список / Административная ответственность / Уровень 2",
  },
  {
    code: "NBA3",
    description:
      "Негатив / Черный список / Административная ответственность / Уровень 3",
  },
  {
    code: "NBDA",
    description:
      "Негатив / Черный список / Смерть / Подтвержденная документально",
  },
  {
    code: "NBDN",
    description:
      "Негатив / Черный список / Смерть / Неподтвержденная документально",
  },
  {
    code: "NBB1",
    description:
      "Негатив / Черный список / Банкротство / Возбуждено дело о банкротстве",
  },
  {
    code: "NBB2",
    description: "Негатив / Черный список / Банкротство / Признан банкротом",
  },
  { code: "NBB", description: "Негатив / Черный список / Предатели Родины" },
  {
    code: "NGC1",
    description: "Негатив / Серый список / Криминал / Неумышленые преступления",
  },
  {
    code: "NGC2",
    description: "Негатив / Серый список / Криминал / Налоговые преступления",
  },
  {
    code: "NF11",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 1 / Операціі 1",
  },
  {
    code: "NF21",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 2 / Операціі 1",
  },
  {
    code: "NF12",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 1 / Операціі 2",
  },
  {
    code: "NF22",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 2 / Операціі 2",
  },
  {
    code: "NF13",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 1 / Операціі 3",
  },
  {
    code: "NF23",
    description:
      "Негатив / Черный список фин. Мониторинга / Рахунки 2 / Операціі 3",
  },
  { code: "NGO", description: "Негатив / Серый список / Жалобы на Банк" },
  { code: "SM", description: "Чуствительная информация / Мобилизован в ЗСУ" },
  { code: "SI", description: "Чуствительная информация / Инсайдер банка" },
  { code: "SP", description: "Чуствительная информация / PEP" },
  {
    code: "ILC",
    description: "Справочная информация / Кредитный продукт / Кредитная карта",
  },
  {
    code: "ILP",
    description: "Справочная информация / Кредитный продукт / POS Кредит",
  },
  {
    code: "ILA",
    description: "Справочная информация / Кредитный продукт / Авто-кредит",
  },
  {
    code: "ILM",
    description: "Справочная информация / Кредитный продукт / Ипотека",
  },
  { code: "ID", description: "Справочная информация / Директор компании" },
  { code: "IZ", description: "Справочная информация / Основатель  компаниии" },
  { code: "IS", description: "Справочная информация / СПД" },
  {
    code: "IOD",
    description: "Справочная информация / Другой продукт / Депозит",
  },
];
const tagShema = Yup.object().shape({
  tag: Yup.string().oneOf(
    tagOptions.map((tag) => tag.code, "Не вірний тип тегу")
  ),
});

const Tags = ({ data, onChange }) => {
  const {
    asOf,
    until,
    source,
    importSources,
    tagType,
    id,
    description,
    eventDate,
    numberValue,
    textValue,
  } = data;
  const [edit, setEdit] = useState(true);
  const [sources, setSources] = useState(false);
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
          <div className={"source-container ml-10 pb-16"}>
            <b className="mr-10">Тег:</b>
            {tagType.description}
            <span className="tag_name">({tagType.code})</span>
            <span
              onClick={() => setSources(!sources)}
              onMouseLeave={() => setTimeout(() => setSources(false), 500)}
              className="ml-10 pointer"
            >
              {importSources && importSources.length > 0
                ? `(${importSources.length} ${sourceName(importSources)})`
                : ""}
            </span>
            {((sources && userRole === "ADVANCED") ||
              (sources && userRole === "ADMIN")) &&
              importSources.map((s) => {
                return (
                  <ul className={"source_w"} key={s.id}>
                    <li>{s.name}</li>
                  </ul>
                );
              })}
          </div>
          <p className={"source-container ml-10"}>
            <b className="mr-10">Опис:</b>
            {description}
          </p>
          <p className={"ml-10"}>
            <b className="mr-10">Дата події:</b>
            {new DateObject(eventDate).format("DD.MM.YYYY")}
          </p>
          <div className="source-container d-flex ml-10 pb-16">
            <span className="mr-10">
              <b className="mr-10">Текстове значення:</b>
              {textValue}
            </span>
          </div>
          <div className="source-container d-flex ml-10 pb-16">
            <span className="mr-10">
              <b className="mr-10">Числове значення:</b>
              {numberValue}
            </span>
          </div>
          <div className="d-flex ml-10">
            <span className="mr-10">
              <b className="mr-10">З:</b>
              {new DateObject(asOf).format("DD.MM.YYYY")}
            </span>
            <span>
              <b className="mr-10">По:</b>
              {until === null
                ? "(Теперішній)"
                : new DateObject(until).format("DD.MM.YYYY")}
            </span>
          </div>
          <small className={"ml-10"}>{source}</small>
        </div>
      ) : (
        <div>
          <div className="card mb-3">
            <div className={"card_m"}>
              <Formik
                initialValues={{
                  tag: tagType.code,
                  until,
                  asOf,
                }}
                validationSchema={tagShema}
                onSubmit={(values) => {
                  setEdit(!edit);
                  console.log(values, "TAG");
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
                    <div className="col-sm-6 mr-10">
                      <label htmlFor="tag">Тег:</label>
                      <select
                        name="tag"
                        id={id}
                        className={`form-select ${
                          touched.tag && errors.tag ? "is-invalid" : ""
                        }`}
                        value={values.tag}
                        onChange={(e) => {
                          onChange(e);
                          handleChange(e);
                        }}
                        onBlur={handleBlur}
                      >
                        {tagOptions.map(({ code, description }) => {
                          return (
                            <option value={code} key={code}>
                              {description} {""}({code})
                            </option>
                          );
                        })}
                      </select>
                      {errors.tag ? (
                        <span className="text-danger">{errors.tag}</span>
                      ) : null}
                    </div>
                    <div className={"d-flex"}>
                      <div className="col-sm-2 mr-10">
                        <label htmlFor="asOf">З:</label>
                        <input
                          name="asOf"
                          id={id}
                          className="form-control"
                          value={values.asOf}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="date"
                        />
                      </div>
                      <div className="col-sm-2 mr-10">
                        <label htmlFor="until">По:</label>
                        <input
                          name="until"
                          id={id}
                          className="form-control"
                          value={values.until}
                          onChange={(e) => {
                            onChange(e);
                            handleChange(e);
                          }}
                          onBlur={handleBlur}
                          type="date"
                        />
                      </div>
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

export default Tags;
