import React, { useState, useEffect } from "react";
import PageTitle from "../../components/PageTitle";
import TableItem from "../../components/ProgressBar/TableItem";
import ShedulerEditModal from "../../components/Modal/ShedulerEditModal";
import authHeader from "../../api/AuthHeader";
import { setAlertMessageThunk } from "../../store/reducers/AuthReducer";
import { useDispatch } from "react-redux";

const groupNames = [
  "otp-etl.scheduler",
  "otp-etl.enricher",
  "otp-etl.downloader",
  "otp-etl.dwh",
  "otp-etl.report",
  "otp-etl.importer",
  "otp-etl.notification",
  "otp-etl.statuslogger",
];

const Sheduler = () => {
  const [data, setData] = useState([]);
  const uniqueArrayGroupName = (obj) => [
    ...new Set(obj.map((o) => o.groupName)),
  ];
  const changedRow = (id) => {
    let selected = id?.split("/");
    const result = data.filter(
      (el) => el.groupName === selected[0] && el.name === selected[1]
    );
    return {
      data: result[0].data,
      enabled: result[0].enabled,
      exchange: result[0].exchange,
      forceDisabled: !result[0].forceDisabled,
      groupName: result[0].groupName,
      name: result[0].name,
      schedule: result[0].schedule,
    };
  };

  const dispatch = useDispatch();
  const uniqueArrayExchange = (obj) => [...new Set(obj.map((o) => o.exchange))];

  const uniqueGroupNames = [...uniqueArrayExchange(data), ...groupNames];
  const unique = () => [...new Set(uniqueGroupNames)];

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
      }).then((res) => {
        if (res.status === 200) {
          fetchSchedule();
          dispatch(setAlertMessageThunk(`Группа активна ${search}`, "success"));
        }
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
      }).then((res) => {
        if (res.status === 200) {
          fetchSchedule();
          dispatch(setAlertMessageThunk(`Группа активна ${search}`, "success"));
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
      }).then((res) => {
        if (res.status === 200) {
          fetchSchedule();
          dispatch(
            setAlertMessageThunk("Сервіс розкладів перезавантажено", "success")
          );
        }
      });
    } catch (error) {
      console.log(error);
    }
  };
  const cName = (rows) => {
    if (rows.forceDisabled && !rows.enabled) {
      return "table-header opasity-30";
    } else if (rows.enabled && rows.forceDisabled) {
      return "table-header activated";
    } else if (rows.enabled && !rows.forceDisabled) {
      return "table-header opasity-30 activated";
    }
  };
  const queue = (id) => {
    let selected = id?.split("/");
    const result = data.filter(
      (el) => el.groupName === selected[0] && el.name === selected[1]
    );
    return result[0].exchange;
  };
  const message = (id) => {
    let selected = id?.split("/");
    const result = data.filter(
      (el) => el.groupName === selected[0] && el.name === selected[1]
    );
    return encodeURIComponent(JSON.stringify(result[0].data));
  };
  const activateImmediately = (e) => {
    const requestOptions = {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization:
          "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
      },
    };
    fetch(
      `/api/rabbit/send?queue=${queue(e.target.id)}&message=${message(
        e.target.id
      )}`,
      requestOptions
    ).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk("Додано до розкладу", "success"));
      } else {
        dispatch(
          setAlertMessageThunk(`${res.status} ${res.statusText}`, "danger")
        );
      }
    });
  };
  const update = (e) => {
    const requestOptions = {
      method: "PUT",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization:
          "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
      },
      body: JSON.stringify(changedRow(e.target.id)),
    };
    fetch("/api/schedule/update", requestOptions).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk("Розклад оновлено", "success"));
        fetchSchedule();
      } else {
        dispatch(
          setAlertMessageThunk(`${res.status} ${res.statusText}`, "danger")
        );
      }
    });
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
            {uniqueArrayGroupName(data)
              .sort()
              .map((option) => {
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
        <table className="table table-bordered">
          <thead>
            <tr>
              <th className="table-header">Назва групи</th>
              <th className="table-header">Шифр завдання</th>
              <th className="table-header">Назва черги сповіщень</th>
              <th className="table-header">Сповіщення</th>
              <th className="table-header">Розклад завдань</th>
              <th id="visible" className="table-header">
                Тимчасово заборонити виконання завдання
              </th>
              <th id="visible" className="table-header">
                Виконати примусово
              </th>
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
                    onClick={(e) => {
                      const { name } = e.target;
                      if (name !== "forceDisabled" && name !== "activate") {
                        setEditRow(row);
                      }
                    }}
                    key={index}
                  >
                    <TableItem item={row.groupName} />
                    <TableItem item={row.name} />
                    <TableItem item={row.exchange} />
                    <TableItem item={JSON.stringify(row.data)} />
                    <TableItem
                      item={
                        row.schedule === null
                          ? "-"
                          : JSON.stringify(row.schedule)
                      }
                    />
                    <td className="text-center align-middle">
                      <input
                        id={row.groupName + "/" + row.name}
                        className="big-checkbox"
                        type="checkbox"
                        checked={row.forceDisabled}
                        name="forceDisabled"
                        onChange={(e) => update(e)}
                      />
                    </td>
                    <td className="text-center align-middle">
                      <button
                        id={row.groupName + "/" + row.name}
                        onClick={(e) => activateImmediately(e)}
                        name="activate"
                        className="btn custom-btn"
                      >
                        Виконати
                      </button>
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
          exchange={unique()}
        />
      )}
    </div>
  );
};
export default Sheduler;
