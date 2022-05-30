import React from "react";
import { DateObject } from "react-multi-date-picker";

const Passports = ({ data }) => {
  const {
    series,
    number,
    authority,
    // issued,
    endDate,
    // recordNumber,
    // type,
    validity,
  } = data;

  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">Паспорт:</b>
        {series}
        {number}
      </p>
      <p>
        <b className="mr-10">Ким виданний:</b>
        {authority}
      </p>

      <p>
        <b className="mr-10">Дата закінчення</b> :{new DateObject(endDate).format("DD.MM.YYYY")}
      </p>
      <p> {validity ? "Дійсний" : "Недійсний"} </p>
    </div>
  );
};

export default Passports;
