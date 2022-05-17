import Multiselect from "multiselect-react-dropdown";
import React from "react";

const MultiselectDay = ({
  name,
  options,
  period,
  // setRowDate,
  setWeekDays,
  selectedValues,
}) => {
  return (
    <div className="form-group form-row">
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
          hidePlaceholder={true}
          emptyRecordMsg="Не знайдeно збігів"
          onSelect={(day) => {
            var value = day.reduce(function (prev, curr) {
              return [...prev, curr.value];
            }, []);
            setWeekDays((prevState) => ({
              ...prevState,
              month: {
                ...prevState.month,
                [name]: value,
              },
            }));
          }}
          onRemove={(day) => {
            var value = day.reduce(function (prev, curr) {
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
                month: {},
              }));
            }
          }}
        />
        <span className="ml-10">тиждень</span>
      </div>
    </div>
  );
};

export default MultiselectDay;
