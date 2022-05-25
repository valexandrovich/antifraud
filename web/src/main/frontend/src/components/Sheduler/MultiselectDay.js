import Multiselect from "multiselect-react-dropdown";
import React from "react";

const MultiselectDay = ({
  name,
  options,
  period,
  setWeekDays,
  selectedValues,
  selectAll,
  removeAll,
}) => {
  return (
    <div className="form-group form-row mb-3">
      <label
        className="d-flex align-items-center justify-content-between "
        htmlFor={name}
      >
        {name}
      </label>
      <div className="select">
        <Multiselect
          className="green"
          displayValue="name"
          options={options}
          disable={period !== "month"}
          selectedValues={selectedValues}
          hidePlaceholder={selectedValues}
          placeholder="-"
          emptyRecordMsg="Не знайдeно збігів"
          onSelect={(day) => {
            var value = day.reduce((prev, curr) => {
              return [...prev, curr.value];
            }, []);
            setWeekDays((prevState) => ({
              ...prevState,
              month: {
                ...prevState.month,
                [name]: value.length === 6 ? "all" : value,
              },
            }));
          }}
          onRemove={(day) => {
            var value = day.reduce((prev, curr) => {
              return [...prev, curr.value];
            }, []);
            if (value.length > 0) {
              setWeekDays((prevState) => ({
                ...prevState,
                month: {
                  ...prevState.month,
                  [name]: value,
                },
              }));
            } else {
              setWeekDays((prevState) => ({
                ...prevState,
                month: {
                  ...prevState.month,
                  [name]: undefined,
                },
              }));
            }
            //  else {
            //   setWeekDays((prevState) => ({
            //     ...prevState,
            //     month: {},
            //   }));
            //   debugger;
            // }
          }}
        />
        <span className="ml-10 mr-10">тиждень</span>
        <div className="d-flex flex-column">
          <button
            onClick={selectAll}
            className="btn btn-sm btn-outline-success"
          >
            Обрати всі
          </button>
          <button onClick={removeAll} className="btn btn-sm btn-outline-danger">
            Видалити всі
          </button>
        </div>
      </div>
    </div>
  );
};

export default MultiselectDay;
