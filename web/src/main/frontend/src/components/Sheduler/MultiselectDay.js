import Multiselect from "multiselect-react-dropdown";
import React from "react";
import * as IoIcons from "react-icons/io";

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
          }}
        />
        <span className="ml-10 mr-10">тиждень</span>
        <div className="d-flex">
          <span onClick={selectAll} className="addAll">

            <IoIcons.IoMdDoneAll />
            <span className="text">Обрати всі</span>
          </span>
          <span onClick={removeAll} className="removeAll">
            <IoIcons.IoMdRemoveCircleOutline />
            <span className="text">Видалити всі</span>
          </span>
        </div>
      </div>
    </div>
  );
};

export default MultiselectDay;
