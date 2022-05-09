import React, { useState } from "react";
import DatePicker, { DateObject } from "react-multi-date-picker";
import TimePicker from "react-multi-date-picker/plugins/time_picker";

const SheduleEditor = ({ data, setData }) => {
  const options = ["pereodic", "set", "once"];

  const [startValue, setStartValue] = useState(
    data.start
      ? new DateObject({
          date: data.start,
        })
      : ""
  );
  const [finishValue, setFinishValue] = useState(
    data.finish
      ? new DateObject({
          date: data.finish,
        })
      : ""
  );

  const saveData = (start, finish) => {
    setData((prevData) => ({
      ...prevData,
      shedule: { ...prevData.shedule, start: start, finish: finish },
    }));
  };

  const handleChange = (index) => (e) => {
    const clonedData = [...period];
    const { name, value, id } = e.target;
    clonedData[index][id][name] = value;
    setPeriod(clonedData);
  };

  const renderInputs = (d) =>
    d.map((el, index) => {
      if (typeof el === "object") {
        return Object.entries(el).map(([key, val]) => {
          return (
            <div key={index} className="row">
              <h3>{key.toUpperCase()}:</h3>
              <div className="form-group col-md-4">
                <label htmlFor="type">Type</label>
                <select
                  id={key}
                  name="type"
                  className="form-select"
                  value={val.type}
                  onChange={handleChange(index)}
                >
                  {options.map((option) => {
                    return <option>{option}</option>;
                  })}
                </select>
              </div>
              <div className="form-group col-md-4">
                <label htmlFor="value">Value</label>
                <input
                  id={key}
                  name="value"
                  className="form-control"
                  type="text"
                  value={val.value}
                  onChange={handleChange(index)}
                />
              </div>
            </div>
          );
        });
      }
    });

  return (
    <div>
      <div></div>
      {renderInputs(period)}
      <hr />
      <div className="row mb-2">
        <div className="form-group col-md-4">
          <label htmlFor="startTime">
            <h3>START TIME:</h3>
          </label>
          <DatePicker
            style={{ height: "50px" }}
            format="YYYY-MM-DDTHH:mm:ss"
            value={startValue}
            onChange={setStartValue}
            plugins={[<TimePicker position="bottom" />]}
          />
        </div>
        <div className="form-group col-md-4">
          <label htmlFor="startTime">
            <h3>FINISH TIME:</h3>
          </label>
          <DatePicker
            style={{ height: "50px" }}
            format="YYYY-MM-DDTHH:mm:ss"
            value={finishValue}
            onChange={setFinishValue}
            plugins={[<TimePicker position="bottom" />]}
          />
        </div>
        <button onClick={() => saveData(startValue, finishValue)}>save</button>
      </div>
    </div>
  );
};
export default SheduleEditor;
