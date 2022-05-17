import React, { useState, useEffect } from "react";
import PageTitle from "../../components/PageTitle";
import TableItem from "../../components/ProgressBar/TableItem";
import ShedulerEditModal from "../../components/Modal/ShedulerEditModal";
import authHeader from "../../api/AuthHeader";

const Sheduler = () => {
  const [data, setData] = useState([]);
  const uniqueArrayGroupName = (obj) => [
    ...new Set(obj.map((o) => o.groupName)),
  ];
  const uniqueArrayExchange = (obj) => [...new Set(obj.map((o) => o.exchange))];
  const [editRow, setEditRow] = useState();

  useEffect(() => {
    fetch("/api/schedule/find", { headers: authHeader() })
      .then((response) => response.json())
      .then((res) => setData(res));
  }, [editRow]);
  const [search, setSearch] = useState("all");

  return (
    <div className="wrapped">
      <PageTitle title={"sheduler"} />
      <div className="form-group col-md-6 mb-3">
        <select
          className="form-select"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        >
          <option value="all">Всі</option>
          {uniqueArrayGroupName(data).map((option) => {
            return (
              <option key={option} value={option}>
                {option}
              </option>
            );
          })}
        </select>
      </div>
      <div className="sroll-x">
        <table className="table table-bordered w-90">
          <thead>
            <tr>
              <th className="table-header">Назва групи</th>
              <th className="table-header">Шифр завдання</th>
              <th className="table-header">Назва черги сповіщень</th>
              <th className="table-header">Сповіщення</th>
              <th className="table-header">Розклад завдань</th>
              <th className="table-header">
                Тимчасово заборонити виконання завдання
              </th>
              <th className="table-header">Завдання обране для виконання</th>
            </tr>
          </thead>
          <tbody>
            {data
              .filter((el) => {
                if (search === "all") {
                  return true;
                }
                return el.groupName === search;
              })
              .map((row, index) => {
                return (
                  <tr
                    onClick={() => {
                      setEditRow(row);
                    }}
                    key={index}
                  >
                    <TableItem item={row.groupName} />
                    <TableItem item={row.name} />
                    <TableItem item={row.exchange} />
                    <TableItem item={JSON.stringify(row.data)} />
                    <TableItem item={JSON.stringify(row.schedule)} />
                    <td>
                      <input
                        className="big-checkbox"
                        type="checkbox"
                        checked={row.forceDisabled}
                        readOnly
                      />
                    </td>
                    <td>
                      <input
                        className="big-checkbox"
                        type="checkbox"
                        checked={row.enabled}
                        readOnly
                      />
                    </td>
                  </tr>
                );
              })}
          </tbody>
        </table>
      </div>
      {editRow && (
        <ShedulerEditModal
          open
          onClose={() => setEditRow(null)}
          edit={editRow}
          groupName={uniqueArrayGroupName(data)}
          exchange={uniqueArrayExchange(data)}
        />
      )}
    </div>
  );
};
export default Sheduler;
