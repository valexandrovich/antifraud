import React from "react";
import PageTitle from "../../components/PageTitle";
import ProgressBar from "./ProgresBar";
import TableItem from "./TableItem";
const mockData = [
  {
    progress: 1,
    unit: "percents",
    name: "Manual import from test.xlsx",
    user: "V.A.Bieloienko",
    started: "14.04.2022 23:14:00",
    finished: "14.04.2022 23:14:17",
    status: "Imported: 1234, Failed: 5",
  },
  {
    progress: 69,
    unit: "percents",
    name: "Manual import from test.xlsx",
    user: "V.A.Bieloienko",
    started: "14.04.2022 23:14:00",
    finished: "14.04.2022 23:14:17",
    status: "Imported: 1234, Failed: 5",
  },
  {
    progress: 152,
    unit: "percents",
    name: "Manual import from test.xlsx",
    user: "V.A.Bieloienko",
    started: "14.04.2022 23:14:00",
    finished: "14.04.2022 23:14:17",
    status: "Imported: 1234, Failed: 5",
  },
  {
    progress: 523322,
    unit: "rows",
    name: "Manual import from test.xlsx",
    user: "V.A.Bieloienko",
    started: "14.04.2022 23:14:00",
    finished: "14.04.2022 23:14:17",
    status: "Imported: 1234, Failed: 5",
  },
];

const Progress = () => {
  return (
    <div className="wrapped">
      <PageTitle title={"progress"} />
      <div className="sroll-x">
        <table className="table table-bordered">
          <thead>
            <tr>
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
            {mockData.map((data, index) => {
              return (
                <tr>
                  {data.unit === "percents" ? (
                    <ProgressBar
                      key={index}
                      bgcolor={"green"}
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
