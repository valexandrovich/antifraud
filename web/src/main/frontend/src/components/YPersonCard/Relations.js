import React from "react";

const Relations = ({ data }) => {
  return (
    <>
      <div className="card mb-3">
        <div>
          {data.map(({ group, people }) => {
            return (
              <div className="card mb-3" key={group.id}>
                <div className={"ml-10"}>
                  <div className={"d-flex"}>
                    <b className="mr-10">ID групи:</b>
                    {group.id}
                    <b className="mr-10 ml-10">Тип групи:</b>
                    {group.type}
                  </div>
                  <hr />
                </div>
                {people.map((person) => {
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
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
};

export default Relations;
