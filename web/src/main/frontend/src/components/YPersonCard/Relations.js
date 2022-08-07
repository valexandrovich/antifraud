import React, { useState } from "react";
import * as IoIcons from "react-icons/io";
import YPersonService from "../../api/YPersonApi";

const Relations = ({ data, personId }) => {
  const [open, setOpen] = useState(false);
  const { type, id } = data;
  const [relatedPerson, setRelatedPerson] = useState([]);

  const fetchData = (id) => {
    YPersonService.relatedGroupPersons(id).then((res) =>
      setRelatedPerson(res.data.filter((el) => el.id !== personId))
    );
  };

  return (
    <>
      <div className="pb-2 ml-10">
        <div
          id={id}
          onClick={() => {
            setOpen(!open);
            !open && fetchData(id);
          }}
          className="d-flex justify-content-start pointer"
        >
          <span className={"mr-10"}>
            {open ? <IoIcons.IoIosArrowUp /> : <IoIcons.IoIosArrowDown />}
          </span>
          <span className={open ? "mb-2" : null}>{type}</span>
        </div>
      </div>
      {open && (
        <div className="card mb-3">
          {relatedPerson.map((person) => {
            return (
              <div key={person.id}>
                <p className={"ml-10"}>
                  <b className="mr-10">ПІБ:</b>
                  {person.lastName} {person.firstName} {person.patName}
                </p>
                <p className={"ml-10"}>
                  <b className="mr-10">Дата народження:</b>
                  {person.birthdate}
                </p>
                <p className={"ml-10"}>
                  <b className="mr-10">Адреса:</b>
                </p>
                <p className={"ml-10"}>
                  <b className="mr-10">Паспорт:</b>
                  {person.passport?.series}
                  {person.passport?.number}
                </p>
                <p className={"ml-10"}>
                  <b className="mr-10">ІПН:</b>
                  {person.inn?.inn}
                </p>
                <hr />
              </div>
            );
          })}
        </div>
      )}
    </>
  );
};

export default Relations;
