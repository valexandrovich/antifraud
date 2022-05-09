import React, { useEffect, useState } from "react";
import PageTitle from "../../components/PageTitle";
import TableItem from "../../components/ProgressBar/TableItem";
import ProgressBar from "../../components/ProgressBar/ProgresBar";
import authHeader from "../../api/AuthHeader";

const Progress = () => {
  const [progress, setProgress] = useState([]);

  const setCheck = async () => {
    try {
      const response = await fetch("/api/statuslogger/find", {
        headers: authHeader(),
      });
      const res = await response.json();
      setProgress(res);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    const interval = setInterval(setCheck, 5000);
    return () => clearInterval(interval);
  }, []);
  return (
    <div className="wrapped">
      <PageTitle title={"progress"} />
      <div className="sroll-x">
        <table className="table table-bordered">
          <thead>
            <tr>
              <td>ID</td>
              <td>Progress</td>
              <td>Unit</td>
              <td>Name</td>
              <td>User</td>
              <td>Started</td>
              <td>Finished</td>
              <td>Status</td>
            </tr>
          </thead>
          <tbody>
            {progress.map((data, index) => {
              return (
                <tr key={index}>
                  <TableItem item={data.id} />
                  {data.unit === "%" ? (
                    <ProgressBar bgcolor={"green"} completed={data.progress} />
                  ) : (
                    <TableItem item={data.progress} />
                  )}

                  <TableItem item={data.unit} />
                  <TableItem item={data.name} />
                  <TableItem item={data.user} />
                  <TableItem item={data.started} />
                  <TableItem item={data.finished} />
                  <TableItem item={data.status} />
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Progress;
