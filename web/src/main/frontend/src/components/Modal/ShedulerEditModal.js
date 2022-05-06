import React, { useState } from "react";
import NullSheduler from "../Sheduler/NullSheduler";

import Modal from "./Modal";

const ShedulerEditModal = ({ open, onClose, edit, groupName, exchange }) => {
  const [rowDate, setRowDate] = useState(edit);
  const [period, setPeriod] = useState(null);
  const [daysOfWeek, setDaysOfWeek] = useState(!!edit.schedule?.days_of_week);

  const [shedule, setShedule] = useState(edit.schedule !== null);
  const toogleShedule = () => {
    setShedule(!shedule);
  };

  const [weeksPeriod, setWeeksPeriod] = useState(
    rowDate.schedule?.weeks?.type === " periodic"
  );

  // console.log(rowDate, "SAVED DATA TO PARENT COMPONENT");
  return (
    <Modal open={open} onClose={onClose}>
      <div className="modal-content">
        <div className="modal-header"></div>

        <div className="modal-body">
          <div className="form-group col-md-4">
            <label htmlFor="group_name">
              Назва групи
              <input
                className="form-control"
                value={rowDate.groupName}
                placeholder={rowDate.groupName}
                onChange={(e) => {
                  const { value } = e.currentTarget;
                  setRowDate((prevState) => ({
                    ...prevState,
                    groupName: value,
                  }));
                }}
                list="group"
                name="group_name"
              />
              <datalist id="group">
                {groupName.map((el) => {
                  return <option value={el} key={el}></option>;
                })}
              </datalist>
            </label>
          </div>
          <div className="form-group col-md-4">
            <label htmlFor="startTime">Шифр завдання</label>
            <input
              type="text"
              name="name"
              className="form-control"
              value={rowDate.name}
              onChange={(e) => {
                const { value } = e.currentTarget;
                setRowDate((prevState) => ({
                  ...prevState,
                  name: value,
                }));
              }}
            />
          </div>
          <div className="form-group col-md-4">
            <label htmlFor="startTime">Назва черги сповіщень</label>
            <select
              name="exchange"
              className="form-select"
              value={rowDate.exchange ? rowDate.exchange : ""}
              onChange={(e) => {
                const value = e.currentTarget.value;
                setRowDate((prevState) => ({
                  ...prevState,
                  exchange: value,
                }));
              }}
            >
              {exchange.map((el, index) => {
                return <option key={index}>{el}</option>;
              })}
            </select>
          </div>
          <div className="form-group col-md-8">
            <label>Сповіщення</label>
            <textarea
              name="data"
              className="form-control"
              rows={2}
              value={JSON.stringify(rowDate.data)}
              onChange={(e) => {
                const value = JSON.parse(e.currentTarget.value);
                setRowDate((prevState) => ({
                  ...prevState,
                  data: value,
                }));
              }}
            />
          </div>

          <div className="form-group d-flex align-items-center">
            <input
              className="big-checkbox"
              name="foce_disable"
              type="checkbox"
              checked={JSON.stringify(rowDate.foceDisable) === "true"}
              onChange={(e) => {
                const checked = e.currentTarget.checked;
                setRowDate((prevState) => ({
                  ...prevState,
                  foceDisable: JSON.parse(checked),
                }));
              }}
            />
            <label htmlFor="foce_disable">
              Тимчасово заборонити виконання завдання
            </label>
          </div>
          <div className="form-group d-flex align-items-center ">
            <input
              className="big-checkbox"
              name="enabled"
              type="checkbox"
              checked={JSON.stringify(rowDate.enabled) === "true"}
              onChange={(e) => {
                const checked = e.currentTarget.checked;
                setRowDate((prevState) => ({
                  ...prevState,
                  enabled: JSON.parse(checked),
                }));
              }}
            />
            <label htmlFor="enabled">Завдання обране для виконання</label>
          </div>
          <div className="d-flex align-items-center">
            <label className="miro-radiobutton d-flex align-items-center">
              <input
                className="big-checkbox"
                type="radio"
                value="0"
                name="radio"
                readOnly={true}
                checked={!shedule}
                onClick={toogleShedule}
              />
              <span>Виконати один раз</span>
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
            <NullSheduler
              rowDate={rowDate}
              setRowDate={setRowDate}
              weeksPeriod={weeksPeriod}
              setWeeksPeriod={setWeeksPeriod}
              period={period}
              setPeriod={setPeriod}
              daysOfWeek={daysOfWeek}
              setDaysOfWeek={setDaysOfWeek}
            />
          )}
        </div>
      </div>

      <div className="modal-footer mt-3">
        <button>Отправить</button>
      </div>
    </Modal>
  );
};

export default ShedulerEditModal;
