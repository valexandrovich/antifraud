import React from "react";

const Relations = () => {
  const mockData = [
    {
      birthdate: "1994-01-23",
      firstName: "ВАЛЕРІЙ",
      id: "32bb5cd8-ee7f-4d81-98be-021357655dbc",
      inn: { id: 2, inn: 2361132685 },
      lastName: "БЄЛОЄНКО",
      passport: {
        id: 53,
        series: "AA",
        number: 123459,
      },
      patName: "ОЛЕКСАНДРОВИЧ",
      relations: {
        id: 1,
        name: "sibling",
      },
    },
    {
      birthdate: "1988-06-29",
      firstName: "АНДРІЙ",
      id: "32bb5cd8-ee7f-4d81-98be-021357655dbb",
      inn: { id: 2, inn: 23613232385 },
      lastName: "ВЕНГЕР",
      passport: {
        id: 54,
        series: "AA",
        number: 111111,
      },
      patName: "БОРИСОВИЧ",
      relations: {
        id: 1,
        name: "sibling",
      },
    },
    {
      birthdate: "1994-01-23",
      firstName: "КИРИЛО",
      id: "32bb5cd8-ee7f-5d81-98be-021357655dbc",
      inn: { id: 2, inn: 22222222222 },
      lastName: "ЧОРНОБРИВЕЦЬ",
      passport: {
        id: 55,
        series: "ББ",
        number: 222222,
      },
      patName: "ОЛЕКСАНДРОВИЧ",
    },
  ];

  return (
    <div className={"wrapped"}>
      <div className="card m-3 d-flex">
        <div className="card_header">
          <div className="text-center">
            <h3>Зв'язки</h3>
          </div>
        </div>
        <div className="card-body">
          <table className="table table-striped table-bordered table-sm">
            <thead>
              <tr>
                <th>Ім'я</th>
                <th>Дата народження</th>
                <th>Паспорт</th>
                <th>ІПН</th>
                <th>Зв'язки</th>
              </tr>
            </thead>
            <tbody>
              {mockData.map((data) => {
                return (
                  <tr key={data.id}>
                    <td>
                      {data.lastName} {data.firstName} {data.patName}
                    </td>
                    <td>{data.birthdate}</td>
                    <td>
                      {data.passport.series}
                      {data.passport.number}
                    </td>
                    <td>{data.inn.inn}</td>
                    <td>
                      <select>
                        <option>тип зв'язків</option>
                        <option>Брат</option>
                        <option>Сестра</option>
                        <option>Чоловік</option>
                        <option>Дружина</option>
                      </select>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Relations;
