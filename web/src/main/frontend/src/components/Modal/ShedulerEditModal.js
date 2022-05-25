import React, { useState, useEffect } from "react";
import { DateObject } from "react-multi-date-picker";
import { useDispatch } from "react-redux";
import { setAlertMessageThunk } from "../../store/reducers/AuthReducer";
import NullSheduler from "../Sheduler/NullSheduler";
import scheduleSettings from "../Sheduler/Settings";

import Modal from "./Modal";

const ShedulerEditModal = ({ open, onClose, edit, groupName, exchange }) => {
  const [rowDate, setRowDate] = useState(edit);
  const [formErrors, setFormErrors] = useState({});
  const dispatch = useDispatch();
  const [minPeriod, setMinPeriod] = useState(null);
  const [formatedDate, setFormatedDate] = useState({
    start: "",
    finish: "",
    day: "",
  });
  const [json, setJson] = useState(JSON.stringify(rowDate.data));
  const [finishTime, setFinishTime] = useState(!!rowDate.schedule?.finish);
  const [shedule, setShedule] = useState(rowDate.schedule !== null);
  const [jsonErr, setJsonErr] = useState(false);
  const toogleShedule = () => {
    setShedule(!shedule);
  };
  const validJson = (val) => {
    try {
      JSON.parse(val);
      setRowDate((prevState) => ({
        ...prevState,
        data: JSON.parse(val),
      }));
      setJsonErr(false);
    } catch (error) {
      setJsonErr("Некоректний JSON об'ект");
    }
  };

  const [period, setPeriod] = useState(null);
  const [monthPeriod, setmonthPeriod] = useState(null);
  const [weekDays, setWeekDays] = useState({
    weeks: {},
    month: {},
  });
  const [time, setTime] = useState({
    periodic: "0h0m",
    once: { type: "once", value: "00:00" },
  });

  //  Preset period and data
  const selectedPeriod = () => {
    if (
      rowDate.schedule?.weeks &&
      rowDate.schedule?.weeks.type === "periodic" &&
      rowDate.schedule?.weeks.value > 1
    ) {
      setPeriod("weeks");
    }
    if (
      rowDate.schedule?.month &&
      (rowDate.schedule?.month.type === "once" ||
        rowDate.schedule?.month.type === "set")
    ) {
      setPeriod("month");
    }
    if (rowDate.schedule?.days_of_month) {
      setPeriod("month");
      setmonthPeriod("daymonth");
    }
    if (!rowDate.schedule?.weeks && !rowDate.schedule?.month) {
      if (rowDate.schedule?.days?.type !== "periodic") {
        setRowDate((prevState) => ({
          ...prevState,
          schedule: {
            ...prevState.schedule,
            days: {
              type: "periodic",
              value:
                rowDate.schedule?.days?.value > 0
                  ? rowDate.schedule?.days?.value
                  : 1,
            },
            minutes: {
              type: "once",
              value: "00:01",
            },
          },
        }));
      }
      setPeriod("days");
      setMinPeriod("once");
    }
    if (
      !rowDate.schedule?.month &&
      !rowDate.schedule.days_of_month &&
      rowDate.schedule?.days_of_week
    ) {
      const daysArray = Object.entries(rowDate.schedule?.days_of_week);
      const filtered = daysArray.filter(([_key, value]) => value === "all");
      const daysAll = Object.fromEntries(filtered);
      if (daysAll) {
        setWeekDays((prevState) => ({
          ...prevState,
          weeks: daysAll,
        }));
        setRowDate((prevState) => ({
          ...prevState,
          schedule: {
            ...prevState.schedule,
            weeks: {
              type: "periodic",
              value: 1,
            },
          },
        }));
        setPeriod("weeks");
      }
      if (Object.keys(daysAll).length === 0) {
        setWeekDays((prevState) => ({
          ...prevState,
          month: rowDate.schedule?.days_of_week,
        }));
        setPeriod("month");
        setmonthPeriod("dayweek");
      }
    }
    if (
      rowDate.schedule?.month &&
      (rowDate.schedule?.month.type === "set" ||
        rowDate.schedule?.month.type === "once")
    ) {
      setWeekDays((prevState) => ({
        ...prevState,
        month: rowDate.schedule?.days_of_week,
      }));
      setPeriod("month");
      setmonthPeriod("dayweek");
    }
    if (rowDate.schedule.days_of_month) {
      setPeriod("month");
      setmonthPeriod("daymonth");
    }
    if (rowDate.schedule?.minutes) {
      if (rowDate.schedule?.minutes.type === "periodic") {
        setMinPeriod("periodic");
        setTime((prevState) => ({
          ...prevState,
          periodic: {
            type: rowDate.schedule?.minutes.type,
            value: rowDate.schedule?.minutes.value,
          },
        }));
      }
      if (
        rowDate.schedule?.minutes.type === "set" ||
        rowDate.schedule?.minutes.type === "once"
      ) {
        setMinPeriod("once");
        setTime((prevState) => ({
          ...prevState,
          once: {
            type: rowDate.schedule?.minutes.type,
            value: rowDate.schedule?.minutes.value,
          },
        }));
      }
    }
  };

  useEffect(() => {
    selectedPeriod();
  }, []);

  //  Preset period and data

  useEffect(() => {
    const start = new DateObject(rowDate.schedule?.start);
    const finish = new DateObject(rowDate.schedule?.finish);

    setFormatedDate((prevState) => ({
      ...prevState,
      start: start.format("YYYY-MM-DDTHH:mm"),
      finish: finish.format("YYYY-MM-DDTHH:mm"),
    }));
  }, [rowDate.schedule.start, rowDate.schedule.finish]);

  useEffect(() => {
    setFormErrors(
      scheduleSettings.validate(rowDate, weekDays, time, monthPeriod)
    );
  }, [rowDate, weekDays, time, monthPeriod]);
  const handleValidate = (e) => {
    e.preventDefault();
    const days = () => {
      if (period === "days" && rowDate.schedule?.days?.value === 1) {
        return undefined;
      } else {
        return rowDate.schedule?.days;
      }
    };
    const minutes = () => {
      if (minPeriod !== "periodic") {
        return { type: time.once.type, value: time.once.value };
      }
      if (minPeriod === "periodic" && time.periodic.value.indexOf("h") > 0) {
        if (time.periodic.value && !Number.isInteger(time.periodic.value)) {
          let t = time.periodic.value.split("h");
          if (Number(t[0]) > 0 && Number(t[1].slice(0, -1)) > 0) {
            return {
              type: "periodic",
              value: `${t[0]}h${t[1].slice(0, -1)}`,
            };
          }
          if (Number(t[0]) > 0 && Number(t[1].slice(0, -1)) === 0) {
            return {
              type: "periodic",
              value: `${t[0]}h`,
            };
          }
          if (Number(t[0]) <= 0) {
            return {
              type: "periodic",
              value: Number(`${t[1].slice(0, -1)}`),
            };
          }
        }
      }
      if (Number.isInteger(time.periodic.value)) {
        return { type: "periodic", value: time.periodic.value };
      }
    };
    const weeks = () => {
      if (period === "weeks" && rowDate.schedule.weeks.value === 1) {
        return undefined;
      }
      if (period === "weeks" && rowDate.schedule.weeks.value > 1) {
        return { type: "periodic", value: rowDate.schedule.weeks.value };
      }
    };
    const month = () => {
      if (
        period === "month" &&
        (rowDate.schedule?.month?.value?.length > 0 ||
          rowDate.schedule?.month?.value === 1) &&
        rowDate.schedule?.month?.value?.length !== 12
      ) {
        return rowDate.schedule.month;
      }
      if (period === "month" && rowDate.schedule?.month?.value?.length === 12) {
        return undefined;
      }
    };
    const dayOfMonth = () => {
      if (
        period === "month" &&
        monthPeriod === "daymonth" &&
        rowDate.schedule.days_of_month.value.length !== 31 &&
        rowDate.schedule.days_of_month.value.length > 0
      ) {
        return rowDate.schedule.days_of_month;
      }
      if (
        period === "month" &&
        monthPeriod === "daymonth" &&
        rowDate.schedule.days_of_month.value.length === 31
      ) {
        return undefined;
      }
    };
    const isAll = (val) => val === "all";
    const allEmpty = (val) => val === undefined;
    const daysOfWeek = () => {
      if (
        period === "month" &&
        monthPeriod === "dayweek" &&
        Object.keys(weekDays?.month).length > 0 &&
        !Object.values(weekDays.month).every(allEmpty)
      ) {
        return weekDays.month;
      }
      if (
        period === "month" &&
        monthPeriod === "dayweek" &&
        Object.values(weekDays.month).every(isAll) &&
        Object.keys(weekDays.month).length === 7
      ) {
        return undefined;
      }
      if (
        period === "weeks" &&
        rowDate.schedule?.weeks?.value === 1 &&
        Object.keys(weekDays.weeks).length !== 7
      ) {
        return weekDays.weeks;
      }
      if (
        period === "weeks" &&
        rowDate.schedule?.weeks?.value > 1 &&
        // Object.values(weekDays.weeks).every(isAll) &&
        Object.keys(weekDays.weeks).length !== 7
      ) {
        return weekDays.weeks;
      }
      if (
        period === "month" &&
        monthPeriod === "dayweek" &&
        Object.values(weekDays.month).every(allEmpty)
      ) {
        return undefined;
      }
    };

    let data = {
      enabled: rowDate.enabled,
      data: rowDate.data,
      exchange: rowDate.exchange,
      forceDisabled: rowDate.forceDisabled,
      groupName: rowDate.groupName,
      name: rowDate.name,
      schedule: shedule
        ? {
            start: formatedDate.start,
            finish: finishTime ? formatedDate.finish : undefined,
            minutes: minutes(),
            days: period === "days" ? days() : undefined,
            weeks: period === "weeks" ? weeks() : undefined,
            month: period === "month" ? month() : undefined,
            days_of_month: dayOfMonth(),
            days_of_week: daysOfWeek(),
          }
        : null,
    };

    const requestOptions = {
      method: "PUT",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization:
          "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
      },
      body: JSON.stringify(data),
    };
    fetch("/api/schedule/update", requestOptions).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk("Розклад оновлено", "success"));
        onClose();
      } else {
        dispatch(
          setAlertMessageThunk(`${res.status} ${res.statusText}`, "danger")
        );
      }
    });
  };

  return (
    <Modal
      title={`Редактор розкладу: ${rowDate.groupName}/${rowDate.name} `}
      open={open}
      onClose={onClose}
    >
      <div className="modal-content">
        <div className="modal-body">
          <div className="form-group col-md-4 mb-2">
            <label htmlFor="group_name">
              Назва групи
              <input
                value={rowDate.groupName}
                readOnly
                className={`form-control ${
                  formErrors.groupName ? "is-invalid" : ""
                }`}
                list="group"
                name="group_name"
              />
              <datalist id="group">
                {groupName.map((el) => {
                  return <option value={el} key={el}></option>;
                })}
              </datalist>
            </label>
            {formErrors.groupName && (
              <p className="text-danger">{formErrors.groupName}</p>
            )}
          </div>
          <div className="form-group col-md-4 mb-2">
            <label htmlFor="name">
              Шифр завдання
              <input
                type="text"
                name="name"
                className={`form-control ${
                  formErrors.name ? "is-invalid" : ""
                }`}
                value={rowDate.name}
                readOnly
              />
            </label>
            {formErrors.name && (
              <p className="text-danger">{formErrors.name}</p>
            )}
          </div>
          <div className="form-group col-md-4 mb-2">
            <label htmlFor="exchange">
              Назва черги сповіщень
              <input
                className={`form-control ${
                  formErrors.exchange ? "is-invalid" : ""
                }`}
                placeholder={rowDate.exchange}
                onChange={(e) => {
                  const { value } = e.currentTarget;
                  setRowDate((prevState) => ({
                    ...prevState,
                    exchange: value,
                  }));
                }}
                list="exchange"
                name="exchange"
              />
              <datalist id="exchange">
                {exchange.map((el) => {
                  return <option value={el} key={el}></option>;
                })}
              </datalist>
            </label>
            {formErrors.exchange && (
              <p className="text-danger">{formErrors.exchange}</p>
            )}
          </div>
          <div className="form-group col-md-6 mb-2">
            <label>Сповіщення</label>
            <textarea
              name="data"
              className={`form-control ${jsonErr ? "is-invalid" : ""}`}
              rows={2}
              value={json}
              onChange={(e) => {
                const { value } = e.target;
                setJson(value);
                validJson(value);
              }}
            />
            {jsonErr && <p className="text-danger">{jsonErr}</p>}
          </div>

          <div className="form-group d-flex align-items-center mb-2 ">
            <input
              className="big-checkbox"
              name="foce_disable"
              type="checkbox"
              checked={JSON.stringify(rowDate.forceDisabled) === "true"}
              onChange={(e) => {
                const checked = e.currentTarget.checked;
                setRowDate((prevState) => ({
                  ...prevState,
                  forceDisabled: JSON.parse(checked),
                }));
              }}
            />
            <label htmlFor="foce_disable">
              Тимчасово заборонити виконання завдання
            </label>
          </div>

          <div className="d-flex align-items-center mb-3">
            <label className="miro-radiobutton d-flex align-items-center">
              <input
                className="big-checkbox"
                type="radio"
                value="0"
                name="radio"
                readOnly
                checked={!shedule}
                onClick={toogleShedule}
              />

              <span className="mr-10">Виконати один раз</span>
            </label>
            <label className="miro-radiobutton d-flex align-items-center">
              <input
                className="big-checkbox"
                type="radio"
                value="1"
                name="radio"
                checked={shedule}
                onClick={toogleShedule}
                readOnly
              />
              <span>Виконувати за розкладом</span>
            </label>
          </div>
          {shedule && (
            <div className="card">
              <div className="card-body">
                <NullSheduler
                  rowDate={rowDate}
                  setRowDate={setRowDate}
                  period={period}
                  setPeriod={setPeriod}
                  finishTime={finishTime}
                  setFinishTime={setFinishTime}
                  formErrors={formErrors}
                  minPeriod={minPeriod}
                  setMinPeriod={setMinPeriod}
                  weekDays={weekDays}
                  setWeekDays={setWeekDays}
                  monthPeriod={monthPeriod}
                  setmonthPeriod={setmonthPeriod}
                  time={time}
                  setTime={setTime}
                />
              </div>
            </div>
          )}
        </div>
      </div>
      <div className="modal-footer mt-3">
        <button
          disabled={jsonErr || Object.values(formErrors).length > 0}
          className="btn custom-btn"
          type="submit"
          onClick={(e) => handleValidate(e)}
        >
          Оновити
        </button>
      </div>
    </Modal>
  );
};

export default ShedulerEditModal;
