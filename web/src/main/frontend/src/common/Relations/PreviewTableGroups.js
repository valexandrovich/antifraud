import React from "react";

const PreviewTableGroups = ({ name, group, setSelectedGroup }) => {
  return (
    <div className="card-body">
      <div className={"d-flex align-items-center"}>
        <input
          onClick={(e) => setSelectedGroup(e.currentTarget.value)}
          className={"big-checkbox mr-10"}
          value={group.group.id}
          type={"radio"}
          name={"radio"}
        />
        <h1>{name}</h1>
      </div>

      <table className="table table-striped table-bordered table-sm">
        <thead>
          <tr>
            <th>Ім'я</th>
            <th>Дата народження</th>
            <th>Паспорт</th>
            <th>ІПН</th>
            <th>Адреса</th>
          </tr>
        </thead>
        <tbody>
          {group?.people?.map((people) => {
            return (
              <tr key={people.id}>
                <td>
                  {people.lastName} {people.firstName} {people.patName}
                </td>
                <td>{people.birthdate}</td>
                <td>
                  {people.passport?.series}
                  {people.passport?.number}
                </td>
                <td>{people.inn?.inn}</td>
                <td>{people.address?.address}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
export default PreviewTableGroups;
