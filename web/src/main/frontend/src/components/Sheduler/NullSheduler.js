import React, { useEffect, useState } from "react";
import DatePicker, { DateObject } from "react-multi-date-picker";
import TimePicker from "react-multi-date-picker/plugins/time_picker";
import Multiselect from "multiselect-react-dropdown";

const options = [
  "monday",
  "tuesday",
  "wednesday",
  "thursday",
  "friday",
  "saturday",
  "sunday",
];
const datOptions = [
  { name: "перший", value: "1" },
  { name: "другий", value: "2" },
  { name: "третій", value: "3" },
  { name: "четвертий", value: "4" },
  { name: "останній", value: "-1" },
  { name: "передостанній", value: "-2" },
  { name: "всі", value: "all" },
];
const days = [
  "1",
  "2",
  "3",
  "4",
  "5",
  "6",
  "7",
  "8",
  "9",
  "10",
  "11",
  "12",
  "13",
  "14",
  "15",
  "16",
  "17",
  "18",
  "19",
  "20",
  "21",
  "22",
  "23",
  "24",
  "25",
  "25",
  "26",
  "27",
  "28",
  "-1",
  "-2",
  "-3",
];

const NullSheduler = ({
  rowDate,
  setRowDate,
  weeksPeriod,
  setWeeksPeriod,
  period,
  setPeriod,
  setDaysOfWeek,
  daysOfWeek,
}) => {
  const [min, setMin] = useState("");
  const [hour, setHour] = useState("");
  const weeks = ["", "", "", "", "", "", ""];
  const handleDaysWeek = (e) => {
    const { name, value } = e.target;
    setRowDate((prevState) => ({
      ...prevState,
      schedule: {
        ...prevState.schedule,
        days_of_week: {
          ...prevState.schedule.days_of_week,
          [name]: value,
        },
      },
    }));
  };

  const formatTime = (time) => {
    if (typeof time === "string") {
      time.toLowerCase();
      let withHours = time.indexOf("h");
      if (withHours >= 0) {
        let hours = parseInt(time.substring(0, withHours));
        setHour(hours);
        setMin(time.substring(withHours + 1));
      }
      if (time.endsWith("m")) {
        setMin(time.substring(2, time.length - 1));
      }
    }
    if (Number.isInteger(time)) {
      if (time > 60) {
        setHour((time / 60) ^ 0);
        setMin(time % 60);
      } else {
        setMin(time);
      }
    }
  };

  useEffect(() => {
    formatTime(rowDate.schedule.minutes.value);
  }, [rowDate.schedule.minutes]);

  return (
    <>
      <div className="row">
        <div className="form-group col-md-4">
          <label htmlFor="startTime">Початок</label>
          <DatePicker
            editable={false}
            style={{ height: "50px" }}
            format="YYYY-MM-DDTHH:mm"
            value={
              rowDate.schedule?.start
                ? new DateObject({
                    date: rowDate.schedule.start,
                  })
                : ""
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
        <div className="form-group col-md-4 d-flex align-items-center">
          <label htmlFor="finishTime">Кінець</label>
          <DatePicker
            editable={false}
            style={{ height: "50px" }}
            format="YYYY-MM-DDTHH:mm"
            value={
              rowDate.schedule?.finish
                ? new DateObject({
                    date: rowDate.schedule.finish,
                  })
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
        </div>
      </div>
      {/* Повторювати по днях */}
      <div>
        <div>
          <label className="miro-radiobutton d-flex align-items-center">
            <input
              className="big-checkbox mr-3"
              type="radio"
              value="0"
              name="period"
              onChange={(e) => setPeriod(e.target.value)}
              checked={period === "0"}
            />
            <span>Повторювати кожен/кожні</span>
          </label>
          {period === "0" && (
            <DatePicker
              className="days"
              editable={false}
              hideMonth={true}
              hideYear={true}
              weekDays={weeks}
              value={
                rowDate.schedule?.days
                  ? new DateObject({
                      date: rowDate.schedule.days.value,
                    })
                  : ""
              }
              format="DD"
              style={{ height: "50px" }}
              name="days"
              onChange={(ref) => {
                setRowDate((prevState) => ({
                  ...prevState,
                  schedule: {
                    ...prevState.schedule,
                    days: { type: "periodic", value: ref },
                  },
                }));
              }}
            />
          )}
          день/днів
        </div>
        {/* ПО ТИЖНЯХ */}
        <div>
          <label className="miro-radiobutton d-flex align-items-center">
            <input
              className="big-checkbox mr-3"
              type="radio"
              value="1"
              name="period"
              onChange={(e) => {
                setPeriod(e.target.value);
                setRowDate((prevState) => ({
                  ...prevState,
                  schedule: {
                    ...prevState.schedule,
                    weeks: {
                      type: "periodic",
                      value: rowDate.schedule?.weeks?.value
                        ? rowDate.schedule.weeks.value
                        : "1",
                    },
                  },
                }));
              }}
              checked={period === "1"}
            />

            <span>Повторювати кожен тиждень/тижнів</span>
          </label>
          {period === "1" && (
            <>
              <div className="form-group col-md-4 d-flex align-items-center">
                <label
                  className="d-flex align-items-center"
                  htmlFor="weeks-set"
                >
                  <select
                    value={rowDate.schedule.weeks?.value}
                    onChange={(e) => {
                      const { value } = e.target;
                      setRowDate((prevState) => ({
                        ...prevState,
                        schedule: {
                          ...prevState.schedule,
                          weeks: { type: "periodic", value: value },
                        },
                      }));
                    }}
                    className="form-select"
                  >
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                  </select>
                  Тиждень
                </label>
              </div>
              <div>
                <label className="d-flex align-items-center" htmlFor="weeks">
                  <input
                    title="days_of_weeks"
                    name="days_of_weeks"
                    className="big-checkbox"
                    type="checkbox"
                    checked={!weeksPeriod}
                    onChange={() => setWeeksPeriod(!weeksPeriod)}
                  />
                  {!weeksPeriod
                    ? `Повторювати періодично ${rowDate.schedule.weeks?.value} раз/разів в тиждень `
                    : "Повторювати кожен день тижня/кожні дні тижня"}
                </label>
                {weeksPeriod && (
                  <label
                    className="d-flex align-items-center"
                    htmlFor="weeks-periodic"
                  >
                    Дні тижня
                    <Multiselect
                      isObject={false}
                      options={options}
                      selectedValues={
                        rowDate.schedule.days_of_week
                          ? Object.keys(rowDate.schedule.days_of_week)
                          : ""
                      }
                      placeholder="Оберіть дні тижня"
                      hidePlaceholder={true}
                      emptyRecordMsg="Не знайдeно збігів"
                      onSelect={(day) => {
                        const days_of_week = day.reduce((acc, item) => {
                          acc[item] = "all";
                          return acc;
                        }, {});
                        setRowDate((prevState) => ({
                          ...prevState,
                          schedule: {
                            ...prevState.schedule,
                            days_of_week,
                          },
                        }));
                      }}
                      onRemove={(day) => {
                        const days_of_week = day.reduce((acc, item) => {
                          acc[item] = item;
                          return acc;
                        }, {});
                        setRowDate((prevState) => ({
                          ...prevState,
                          schedule: {
                            ...prevState.schedule,
                            days_of_week,
                          },
                        }));
                      }}
                    />
                  </label>
                )}
              </div>
            </>
          )}
        </div>
        {/* ПО МІСЯЦЯХ */}
        <>
          <label className="miro-radiobutton d-flex align-items-center">
            <input
              className="big-checkbox mr-3"
              type="radio"
              value="2"
              name="period"
              onChange={(e) => setPeriod(e.target.value)}
              checked={period === "2"}
            />
            <span>По місяцях</span>
          </label>
          {period === "2" && (
            <>
              <div className="form-group col-md-4 d-flex flex-column">
                <div className="d-flex">
                  <label
                    className="d-flex align-items-center"
                    htmlFor="daysWeek"
                  >
                    <input
                      name="daysWeek"
                      className="big-checkbox"
                      type="checkbox"
                      checked={daysOfWeek}
                      onChange={() => setDaysOfWeek(!daysOfWeek)}
                    />
                    {!daysOfWeek ? "Дні тижнів місяця" : "Дні місяця"}
                  </label>
                </div>
                {!daysOfWeek && (
                  <label
                    className="d-flex align-items-center"
                    htmlFor="weeks-set"
                  >
                    По місяцях
                    <Multiselect
                      isObject={false}
                      options={days}
                      selectedValues={
                        rowDate.schedule?.days_of_month
                          ? rowDate.schedule.days_of_month?.set ||
                            rowDate.schedule.days_of_month?.once
                          : ""
                      }
                      placeholder="Оберіть дні місяця"
                      hidePlaceholder={true}
                      emptyRecordMsg="Не знайдeно збігів"
                      onSelect={(month) => {
                        if (month.length > 1) {
                          setRowDate((prevState) => ({
                            ...prevState,
                            schedule: {
                              ...prevState.schedule,
                              days_of_month: {
                                set: month,
                              },
                            },
                          }));
                        } else if (month.length === 1) {
                          setRowDate((prevState) => ({
                            ...prevState,
                            schedule: {
                              ...prevState.schedule,
                              days_of_month: {
                                once: month[0],
                              },
                            },
                          }));
                        }
                      }}
                      onRemove={(month) => {
                        if (month.length > 1) {
                          setRowDate((prevState) => ({
                            ...prevState,
                            schedule: {
                              ...prevState.schedule,
                              days_of_month: {
                                set: month,
                              },
                            },
                          }));
                        } else if (month.length === 1) {
                          setRowDate((prevState) => ({
                            ...prevState,
                            schedule: {
                              ...prevState.schedule,
                              days_of_month: {
                                once: month[0],
                              },
                            },
                          }));
                        }
                      }}
                    />
                  </label>
                )}
              </div>
              {daysOfWeek && (
                <div className="col-md-4 d-flex align-items-center flex-column">
                  <div className="form-group  col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="monday"
                    >
                      Понеділок
                      <select
                        value={rowDate.schedule?.days_of_week?.monday}
                        onChange={handleDaysWeek}
                        name="monday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="tuesday"
                    >
                      Вівторок
                      <select
                        value={rowDate.schedule?.days_of_week?.tuesday}
                        onChange={handleDaysWeek}
                        name="tuesday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="wednesday"
                    >
                      Середа
                      <select
                        value={rowDate.schedule?.days_of_week?.wednesday}
                        onChange={handleDaysWeek}
                        name="wednesday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="thursday"
                    >
                      Четверг
                      <select
                        value={rowDate.schedule?.days_of_week?.thursday}
                        onChange={handleDaysWeek}
                        name="thursday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="friday"
                    >
                      П'ятниця
                      <select
                        value={rowDate.schedule?.days_of_week?.friday}
                        onChange={handleDaysWeek}
                        name="friday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="saturday"
                    >
                      Субота
                      <select
                        value={rowDate.schedule?.days_of_week?.saturday}
                        onChange={handleDaysWeek}
                        name="saturday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                  <div className="form-group col-md-3 w-100">
                    <label
                      className="d-flex align-items-center"
                      htmlFor="sunday"
                    >
                      Неділя
                      <select
                        value={rowDate.schedule?.days_of_week?.sunday}
                        onChange={handleDaysWeek}
                        name="sunday"
                        className="form-select"
                      >
                        {datOptions.map((el) => {
                          return (
                            <option key={el.value} value={el.value}>
                              {el.name}
                            </option>
                          );
                        })}
                      </select>
                      тиждень
                    </label>
                  </div>
                </div>
              )}
            </>
          )}
        </>
      </div>
      <div>
        <label className="miro-radiobutton d-flex align-items-center">
          <span>За періодом</span>
          <div className="form-group col-md-1">
            <label className="d-flex align-items-center" htmlFor="hours">
              <input
                value={hour}
                className="form-control"
                name="hours"
                type="number"
                max={12}
                min={0}
                onChange={(e) => {
                  setHour(e.target.value);
                }}
              />
              Годин
            </label>
          </div>
          <div className="form-group col-md-2">
            <label className="d-flex align-items-center" htmlFor="min">
              <input
                value={min}
                className="form-control"
                name="min"
                type="number"
                max={60}
                min={0}
                onChange={(e) => {
                  setMin(e.target.value);
                }}
              />
              Хвилин
            </label>
          </div>
        </label>
      </div>
      <div className="row">
        <button
          onClick={() => {
            setRowDate((prevState) => ({
              ...prevState,
              schedule: {
                ...prevState.schedule,
                minutes: {
                  type: "periodic",
                  value: `${hour}h${min}m`,
                },
              },
            }));
          }}
        >
          Збегірти
        </button>
      </div>
    </>
  );
};

export default NullSheduler;
