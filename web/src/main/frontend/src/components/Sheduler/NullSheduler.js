import React, { useEffect, useState } from "react";
import DatePicker, { DateObject } from "react-multi-date-picker";
import TimePicker from "react-multi-date-picker/plugins/time_picker";
import Multiselect from "multiselect-react-dropdown";
import scheduleSettings from "./Settings";
import "react-multi-date-picker/styles/colors/green.css";
import MultiselectDay from "./MultiselectDay";

const NullSheduler = ({
  rowDate,
  setRowDate,
  period,
  setPeriod,
  setFinishTime,
  finishTime,
  formErrors,
  setMinPeriod,
  minPeriod,
  setWeekDays,
  weekDays,
  setmonthPeriod,
  monthPeriod,
  time,
  setTime,
}) => {
  const [min, setMin] = useState(0);
  const [hour, setHour] = useState(0);
  const selectAll = (name) => {
    setWeekDays((prevState) => ({
      ...prevState,
      month: {
        ...prevState.month,
        [name]: "all",
      },
    }));
  };
  const removeAll = (name) => {
    setWeekDays((prevState) => ({
      ...prevState,
      month: {
        ...prevState.month,
        [name]: [],
      },
    }));
  };

  const getMonth = (v) => {
    if (v && v.type === "set") {
      return v.value;
    }
    if (v && v.type === "once") {
      return new Array(1).fill(v.value);
    }
  };
  const getDayMonth = (d) => {
    if (d && d.type === "set") {
      return d.value;
    }
    if (d && d.type === "once") {
      return new Array(1).fill(d.value);
    }
  };

  const [monthDaysVal, setMonthDaysVal] = useState(
    rowDate.schedule?.days_of_month
      ? getDayMonth(rowDate.schedule?.days_of_month)
      : []
  );

  const monthVal = rowDate.schedule?.month
    ? getMonth(rowDate.schedule.month)
    : [];

  const formatTime = (times) => {
    if (typeof times === "string") {
      times.toLowerCase();
      let withHours = times.indexOf("h");
      if (withHours >= 0) {
        let hours = parseInt(times.substring(0, withHours));
        setHour(hours);
        setMin(times.substring(withHours + 1));
        times = times.substring(withHours + 1);
      }
      if (times.endsWith("m")) {
        setMin(times.substring(0, times.length - 1));
        time = times.substr(0, times.length - 1);
      }
    }
    if (Number.isInteger(times)) {
      setMin(times);
    }
  };

  useEffect(() => {
    formatTime(time.periodic.value);
  }, [time.periodic.value]);
  const select = (name) => {
    return weekDays.month && weekDays.month[name] === "all"
      ? scheduleSettings.datOptions
      : "";
  };
  return (
    <>
      <div className="row">
        <div className="form-group col-md-4">
          <span className="mr-10">Початок</span>
          <DatePicker
            locale={scheduleSettings.ua}
            className="green"
            editable={false}
            style={{ height: "50px" }}
            format="YYYY-MM-DDTHH:mm"
            value={
              rowDate.schedule?.start
                ? new DateObject(rowDate.schedule.start)
                : new DateObject()
            }
            name="start"
            onChange={(ref) => {
              setRowDate((prevState) => ({
                ...prevState,
                schedule: {
                  ...prevState.schedule,
                  start: ref,
                },
              }));
            }}
            plugins={[<TimePicker position="bottom" hideSeconds />]}
          />
        </div>
        <div className="form-group col-md-4 d-flex align-items-center justify-content-between">
          <label className="d-flex align-items-center" htmlFor="weeks">
            <input
              title="days_of_weeks"
              name="days_of_weeks"
              className="big-checkbox"
              type="checkbox"
              checked={finishTime}
              onChange={() => setFinishTime(!finishTime)}
            />
            <span className="mr-10">Кінець</span>
            <DatePicker
              disabled={!finishTime}
              locale={scheduleSettings.ua}
              className="green"
              editable={false}
              style={{ height: "50px" }}
              format="YYYY-MM-DDTHH:mm"
              value={
                rowDate.schedule?.finish
                  ? new DateObject(rowDate.schedule.finish)
                  : ""
              }
              name="finish"
              onChange={(ref) => {
                setRowDate((prevState) => ({
                  ...prevState,
                  schedule: {
                    ...prevState.schedule,
                    finish: ref,
                  },
                }));
              }}
              plugins={[<TimePicker position="bottom" hideSeconds />]}
            />
          </label>
        </div>
      </div>
      {/* Повторювати по днях */}

      <div>
        <div className="card mt-3 mb-3">
          <div className="card-body">
            <label className="miro-radiobutton d-flex align-items-center">
              <input
                className="big-checkbox"
                type="radio"
                value="days"
                name="period"
                onChange={(e) => setPeriod(e.target.value)}
                checked={period === "days"}
              />
              <span className="mr-10">
                <b>Повторювати кожен/кожні</b>
              </span>
              <label className="d-flex align-items-center" htmlFor="weeks-set">
                <select
                  value={
                    rowDate.schedule?.days
                      ? rowDate.schedule?.days.value
                      : Number(1)
                  }
                  onChange={(e) => {
                    const { value } = e.target;

                    setRowDate((prevState) => ({
                      ...prevState,
                      schedule: {
                        ...prevState.schedule,
                        days: {
                          type: "periodic",
                          value: Number(value),
                        },
                      },
                    }));
                  }}
                  disabled={period !== "days"}
                  className="form-select"
                >
                  {scheduleSettings.daysPeriodic.map((day) => {
                    return (
                      <option value={day} key={day}>
                        {day}
                      </option>
                    );
                  })}
                </select>
              </label>
              <span className="ml-10">
                <b>день/днів</b>
              </span>
            </label>
          </div>
        </div>

        {/* ПО ТИЖНЯХ */}

        <div className="card mt-3 mb-3">
          <div className="card-body">
            <div className="row">
              <div className="miro-radiobutton d-flex align-items-center">
                <input
                  className="big-checkbox mr-3"
                  type="radio"
                  value="weeks"
                  name="period"
                  onChange={(e) => {
                    setPeriod(e.target.value);
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
                  }}
                  checked={period === "weeks"}
                />
                <span className="mr-10">
                  <b>Повторювати кожен/кожні</b>
                </span>

                <label
                  className="d-flex align-items-center"
                  htmlFor="weeks-set"
                >
                  <select
                    disabled={period !== "weeks"}
                    value={rowDate.schedule?.weeks?.value}
                    onChange={(e) => {
                      const { value } = e.target;
                      setRowDate((prevState) => ({
                        ...prevState,
                        schedule: {
                          ...prevState.schedule,
                          weeks: { type: "periodic", value: Number(value) },
                        },
                      }));
                    }}
                    className="form-select"
                  >
                    <option value={1}>1</option>
                    <option value={2}>2</option>
                    <option value={3}>3</option>
                    <option value={4}>4</option>
                  </select>
                </label>
                <span className="ml-10">
                  <b>тиждень/тижнів</b>
                </span>
              </div>
              {period === "weeks" && (
                <div>
                  <label
                    className="d-flex align-items-center mt-3"
                    htmlFor="weeks-periodic"
                  >
                    <span className="mr-10">Дні тижня</span>

                    <Multiselect
                      className="green"
                      isObject={false}
                      options={scheduleSettings.options}
                      selectedValues={
                        weekDays.weeks && Object.keys(weekDays.weeks)
                      }
                      placeholder="Оберіть дні тижня"
                      hidePlaceholder={true}
                      emptyRecordMsg="Не знайдeно збігів"
                      onSelect={(day) => {
                        const weeks = day.reduce((acc, item) => {
                          acc[item] = "all";
                          return acc;
                        }, {});
                        setWeekDays((prevState) => ({
                          ...prevState,
                          weeks,
                        }));
                      }}
                      onRemove={(day) => {
                        const weeks = day.reduce((acc, item) => {
                          acc[item] = "all";
                          return acc;
                        }, {});
                        setWeekDays((prevState) => ({
                          ...prevState,
                          weeks,
                        }));
                      }}
                    />
                  </label>
                </div>
              )}
            </div>
          </div>
        </div>
        {/* ПО МІСЯЦЯХ */}
        <div className="card mt-3 mb-3">
          <div className="card-body">
            <label className="miro-radiobutton d-flex align-items-center mb-3">
              <input
                className="big-checkbox mr-3"
                type="radio"
                value="month"
                name="period"
                onChange={(e) => setPeriod(e.target.value)}
                checked={period === "month"}
              />
              <span className="mr-10">
                <b>По місяцях</b>
              </span>

              <label className="d-flex align-items-center" htmlFor="montch-set">
                <Multiselect
                  disable={period !== "month"}
                  className="green"
                  displayValue="name"
                  options={scheduleSettings.month}
                  selectedValues={
                    monthVal &&
                    scheduleSettings.month?.filter((el) =>
                      monthVal.includes(Number(el.id))
                    )
                  }
                  hidePlaceholder={
                    monthVal &&
                    scheduleSettings.month?.filter((el) =>
                      monthVal.includes(Number(el.id))
                    )
                  }
                  placeholder="Всі місяці"
                  emptyRecordMsg="Не знайдeно збігів"
                  onSelect={(m) => {
                    const data = m.map((acc, item) => {
                      acc[item] = item;
                      return acc;
                    }, {});
                    const val = data.map(({ id }) => id);
                    setRowDate((prevState) => ({
                      ...prevState,
                      schedule: {
                        ...prevState.schedule,
                        month: {
                          type: val.length > 1 ? "set" : "once",
                          value: val.length > 1 ? val : val[0],
                        },
                      },
                    }));
                  }}
                  onRemove={(m) => {
                    const data = m.map((acc, item) => {
                      acc[item] = item;
                      return acc;
                    }, {});
                    const val = data.map(({ id }) => id);
                    setRowDate((prevState) => ({
                      ...prevState,
                      schedule: {
                        ...prevState.schedule,
                        month: {
                          type: val.length > 1 ? "set" : "once",
                          value: val.length > 1 ? val : val[0],
                        },
                      },
                    }));
                  }}
                />
              </label>
            </label>

            {period === "month" && (
              <>
                <div
                  style={{ marginLeft: 50 }}
                  className="d-flex align-items-center mb-3"
                >
                  <label className="miro-radiobutton d-flex align-items-center">
                    <input
                      className="big-checkbox mr-3"
                      type="radio"
                      value="daymonth"
                      name="month"
                      onChange={(e) => setmonthPeriod(e.target.value)}
                      checked={monthPeriod === "daymonth"}
                    />
                    <span className="mr-14">Дні місяця</span>
                  </label>

                  <Multiselect
                    disable={monthPeriod !== "daymonth"}
                    className="green "
                    isObject={false}
                    options={scheduleSettings.days}
                    selectedValues={monthDaysVal}
                    hidePlaceholder={monthDaysVal.length > 0}
                    placeholder="Вcі дні місяця"
                    emptyRecordMsg="Не знайдeно збігів"
                    onSelect={(day) => {
                      setMonthDaysVal(day);
                      setRowDate((prevState) => ({
                        ...prevState,
                        schedule: {
                          ...prevState.schedule,
                          days_of_month: {
                            ...prevState.schedule.days_of_month,
                            type: day.length > 1 ? "set" : "once",
                            value: day.length > 1 ? day : day[0],
                          },
                        },
                      }));
                    }}
                    onRemove={(day) => {
                      setMonthDaysVal(day);
                      setRowDate((prevState) => ({
                        ...prevState,
                        schedule: {
                          ...prevState.schedule,
                          days_of_month: {
                            ...prevState.schedule.days_of_month,
                            type: day.length > 1 ? "set" : "once",
                            value: day.length > 1 ? day : day[0],
                          },
                        },
                      }));
                    }}
                  />
                </div>
                <div style={{ marginLeft: 50 }}>
                  <label className="miro-radiobutton d-flex align-items-center">
                    <input
                      className="big-checkbox mr-3 ml-3"
                      type="radio"
                      value="dayweek"
                      name="month"
                      onChange={(e) => setmonthPeriod(e.target.value)}
                      checked={monthPeriod === "dayweek"}
                    />
                    <span className="mr-10">Дні тижнів місяця</span>
                  </label>

                  {monthPeriod === "dayweek" && (
                    <div className="d-flex flex-column mt-3">
                      {scheduleSettings.options.map((day) => {
                        return (
                          <MultiselectDay
                            name={day}
                            selectAll={() => selectAll(day)}
                            removeAll={() => removeAll(day)}
                            period={period}
                            rowDate={weekDays.month}
                            setWeekDays={setWeekDays}
                            options={scheduleSettings.datOptions}
                            selectedValues={
                              weekDays.month && weekDays.month[day] !== "all"
                                ? scheduleSettings.datOptions.filter((el) =>
                                    weekDays.month[day]?.includes(el.value)
                                  )
                                : select(day)
                            }
                          />
                        );
                      })}
                      {formErrors.days_of_week && (
                        <p className="text-danger">{formErrors.days_of_week}</p>
                      )}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
      <p>Час виконання:</p>
      <div className="card">
        <div className="card-body">
          <div className="mb-3 mt-3">
            <label className="miro-radiobutton d-flex align-items-center">
              <input
                className="big-checkbox"
                type="radio"
                value="periodic"
                name="minperiod"
                onChange={(e) => setMinPeriod(e.target.value)}
                checked={minPeriod === "periodic"}
              />
              <span className="mr-10">За періодом</span>
              <label className="d-flex align-items-center" htmlFor="hours">
                <input
                  disabled={minPeriod !== "periodic"}
                  value={hour}
                  className="form-control mr-10"
                  name="hours"
                  type="number"
                  max={12}
                  min={0}
                  onChange={(e) => {
                    const { value } = e.target;
                    setTime((prevState) => ({
                      ...prevState,
                      periodic: {
                        type: "periodic",
                        value:
                          value <= 23 ? `${value}h${min}m` : `${23}h${min}m`,
                      },
                    }));
                  }}
                />
              </label>
              <span className="mr-10">Годин</span>
              <label className="d-flex align-items-center" htmlFor="min">
                <input
                  value={min}
                  disabled={minPeriod !== "periodic"}
                  className="form-control mr-10"
                  name="min"
                  type="number"
                  max={59}
                  min={0}
                  onChange={(e) => {
                    const { value } = e.target;
                    setTime((prevState) => ({
                      ...prevState,
                      periodic: {
                        type: "periodic",
                        value:
                          value <= 60 ? `${hour}h${value}m` : `${hour}h${59}m`,
                      },
                    }));
                  }}
                />
              </label>
              <span>Хвилин</span>
            </label>
          </div>
          <label className="miro-radiobutton d-flex align-items-center">
            <input
              className="big-checkbox"
              type="radio"
              value="once"
              name="minperiod"
              onChange={(e) => setMinPeriod(e.target.value)}
              checked={minPeriod === "once"}
            />
            <span className="mr-10">В заданний час (через ",")</span>
            <label className="d-flex align-items-center" htmlFor="minOnce">
              <input
                disabled={minPeriod !== "once"}
                value={time.once.value}
                className={`form-control ${
                  formErrors.minutes_once || formErrors.minutes_set
                    ? "is-invalid"
                    : ""
                }`}
                name="minOnce"
                type="text"
                onChange={(e) => {
                  const { value } = e.target;
                  if (value.length <= 5) {
                    setTime((prevState) => ({
                      ...prevState,
                      once: {
                        type: "once",
                        value: value,
                      },
                    }));
                  } else {
                    const arr = value.split(",");
                    setTime((prevState) => ({
                      ...prevState,
                      once: {
                        type: "set",
                        value: arr,
                      },
                    }));
                  }
                }}
              />
            </label>
          </label>
          {formErrors.minutes_once && (
            <p className="text-danger">{formErrors.minutes_once}</p>
          )}
          {formErrors.minutes_set && (
            <p className="text-danger">{formErrors.minutes_set}</p>
          )}
        </div>
      </div>
    </>
  );
};

export default NullSheduler;
