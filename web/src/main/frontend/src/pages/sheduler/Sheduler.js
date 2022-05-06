import React, { useState, useEffect } from "react";
import PageTitle from "../../components/PageTitle";
import TableItem from "../../components/ProgressBar/TableItem";
import ShedulerEditModal from "../../components/Modal/ShedulerEditModal";
import authHeader from "../../api/AuthHeader";

const avaibleExchange = ["otp-etl.downloader", "otp-etl.scheduler"];
const Sheduler = () => {
  const [data, setData] = useState([]);
  const uniqueArray = (obj) => [...new Set(obj.map((o) => o.groupName))];
  useEffect(() => {
    fetch("/api/schedule/find", { headers: authHeader() })
      .then((response) => response.json())
      .then((res) => setData(res));
  }, []);
  const [editRow, setEditRow] = useState();

  return (
    <div className="wrapped">
      <PageTitle title={"sheduler"} />
      <div className="sroll-x">
        <table className="table table-bordered">
          <thead>
            <tr>
              <td>group_name</td>
              <td>name</td>
              <td>exchange</td>
              <td>data</td>
              <td>shedule</td>
              <td>foce_disable</td>
              <td>enabled</td>
            </tr>
          </thead>
          <tbody>
            {data.map((row, index) => {
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
                      type="checkbox"
                      checked={row.forceDisabled}
                      readOnly
                    />
                  </td>
                  <td>
                    <input type="checkbox" checked={row.enabled} readOnly />
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
          groupName={uniqueArray(data)}
          exchange={avaibleExchange}
        />
      )}
    </div>
  );
};
export default Sheduler;
