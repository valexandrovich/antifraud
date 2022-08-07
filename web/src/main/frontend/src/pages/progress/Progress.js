import React, { useCallback, useEffect, useRef, useState } from "react";
import PageTitle from "../../common/PageTitle";
import TableItem from "../../common/TableItem";
import ProgressBar from "../../components/ProgressBar/ProgressBar";
import authHeader from "../../api/AuthHeader";

const Progress = () => {
  const [progress, setProgress] = useState([]);
  const mountedRef = useRef(true);
  const setCheck = useCallback(() => {
    fetch("/api/statuslogger/find", {
      headers: authHeader(),
    })
      .then((response) => response.json())
      .then((res) => {
        if (!mountedRef.current) return null;
        setProgress(res);
      });
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
      <div className="sroll-x tableFixHead">
        <table className="table table-bordered">
          <thead>
            <tr className={"align-middle text-center"}>
              <th className="table-header">ID</th>
              <th className="table-header">Progress</th>
              <th className="table-header">Unit</th>
              <th className="table-header">Name</th>
              <th className="table-header">User</th>
              <th className="table-header">Started</th>
              <th className="table-header">Finished</th>
              <th className="table-header">Status</th>
            </tr>
          </thead>
          <tbody>
            {progress.length > 0 &&
              progress
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
