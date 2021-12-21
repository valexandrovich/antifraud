import React, { useEffect, useState } from "react";
import PageTitle from "../../components/PageTitle";

const DbCheck = () => {
  const [dbData, setDbData] = useState([]);
  const setCheck = async () => {
    const requestOptions = {
      method: "POST",
    };
    const response = await fetch("/db_check", requestOptions);
    const res = await response.json();
    setDbData(res);
  };

  useEffect(() => {
    const interval = setInterval(setCheck, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="wrapped">
      <PageTitle title={"db_check"} />
      {dbData.map((el, idx) => {
        return (
          <>
            <h3 key={idx}>
              {el.tableName} : {el.rowsCount}
            </h3>
          </>
        );
      })}
    </div>
  );
};

export default DbCheck;
