import React, { useCallback, useEffect, useRef, useState } from "react";
import PageTitle from "../../components/PageTitle";
import TableItem from "../../components/ProgressBar/TableItem";
import ProgressBar from "../../components/ProgressBar/ProgressBar";
import authHeader from "../../api/AuthHeader";

const Progress = () => {
  const [progress, setProgress] = useState([]);
  const mountedRef = useRef(true);
  const setCheck = useCallback(() => {
    try {
      fetch("/api/statuslogger/find", {
        headers: authHeader(),
      })
        .then((response) => response.json())
        .then((res) => {
          if (!mountedRef.current) return null;
          if (res.status === 200) {
            setProgress(res);
          }
        });
    } catch (error) {
      console.log(error);
    }
  }, []);

  useEffect(() => {
    setCheck();
    const interval = setInterval(setCheck, 5000);
    return () => {
      clearInterval(interval);
      mountedRef.current = false;
    };
  }, [setCheck]);

  return (
    <div className="wrapped">
      <PageTitle title={"progress"} />
      <div className="sroll-x">
        <table className="table table-bordered">
          <thead>
            <tr>
              <td className="table-header">ID</td>
              <td className="table-header">Progress</td>
              <td className="table-header">Unit</td>
              <td className="table-header">Name</td>
              <td className="table-header">User</td>
              <td className="table-header">Started</td>
              <td className="table-header">Finished</td>
              <td className="table-header">Status</td>
            </tr>
          </thead>
          <tbody>
            {progress
              .sort((a, b) => {
                if ((a.finished === null) !== (b.finished == null))
                  return a.finished === null ? -1 : 1;
                return a.started < b.started ? 1 : -1;
              })
              .map((data, index) => {
                return (
                  <tr key={index}>
                    <TableItem item={data.id} />
                    {data.unit === "%" ? (
                      <ProgressBar
                        bgcolor={"#60aa18"}
                        completed={data.progress}
                      />
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
