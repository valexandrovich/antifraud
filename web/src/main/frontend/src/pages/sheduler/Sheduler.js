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
  const [editRow, setEditRow] = useState(null);
  const fetchSchedule = () => {
    fetch("/api/schedule/find", { headers: authHeader() })
      .then((response) => response.json())
      .then((res) => setData(res));
  };
  useEffect(() => {
    fetchSchedule();
  }, [editRow]);
  const [search, setSearch] = useState("all");
  const exchangeSwitch = async (group) => {
    try {
      await fetch(`/api/schedule/exchangeSwitch/${group}`, {
        method: "POST",
        headers: authHeader(),
      });
    } catch (error) {
      console.log(error);
    }
  };
  const exchangeActivate = async (group) => {
    try {
      await fetch(`/api/schedule/exchangeActivate/${group}`, {
        method: "POST",
        headers: authHeader(),
      }).then(res => {
        if (res.status === 200) {
          fetchSchedule();
        }
      });
    } catch (error) {
      console.log(error);
    }
  };
  const Refresh = async () => {
    try {
      await fetch("/api/schedule/exchangeRefresh", {
        method: "POST",
        headers: authHeader(),
      }).then(res => {
        if (res.status === 200) {
          fetchSchedule();
        }
      });
    } catch (error) {
      console.log(error);
    }
  };
  const cName = (rows) => {
    if (rows.forceDisabled && rows.enabled) {
      return "table-header opasity-30";
    }
    if (rows.enabled && !rows.forceDisabled) {
      return "table-header";
    }
  };
  return (
    <div className="wrapped">
      <PageTitle title={"sheduler"} />
      <div className="row">
        <div className="form-group col-md-6 mb-2">
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
        <div className="col-md-4 mb-2">
          <button
            type="button"
            onClick={() => Refresh()}
            className="btn custom-btn w-100"
          >
            Перезавантажити Sheduler
          </button>
        </div>
      </div>
      {search !== "all" && (
        <div className="row">
          <div className="col-md-4">
            <button
              type="button"
              onClick={() => exchangeActivate(search)}
              className="btn btn-success mb-2 w-100"
            >
              Зробити групу активною
            </button>
          </div>
          <div className="col-md-5">
            <button
              type="button"
              onClick={() => exchangeSwitch(search)}
              className="btn btn-danger mb-2 w-100"
            >
              Зробити групу активною та перезавантажити Sheduler
            </button>
          </div>
        </div>
      )}
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
              .sort((a, b) =>
                a.groupName === b.groupName
                  ? a.name > b.name
                    ? 1
                    : -1
                  : a.groupName > b.groupName
                    ? 1
                    : -1
              )
              .filter((el) => {
                if (search === "all") {
                  return true;
                }
                return el.groupName === search;
              })
              .map((row, index) => {
                return (
                  <tr
                    className={cName(row)}
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
