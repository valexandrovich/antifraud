import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Card from "../../components/Card/Card";
import PageTitle from "../../components/PageTitle";
import Pagination from "../../components/Pagination";
import PerPage from "../../components/PerPage";
import Spinner from "../../components/Loader";

const Monitoring = () => {
  const [subscribed, setSubscribed] = useState([]);
  const [pageSize, setPageSize] = useState(6);
  const [pageNo, setPageNo] = useState(0);
  const [totalFiles, setTotalFiles] = useState();
  const [loader, setLoader] = useState(false);
  const paginate = (pageNumber) => setPageNo(pageNumber - 1);

  useEffect(() => {
    setLoader(true);
    const getSubscribed = async () => {
      try {
        const response = await fetch("/api/user/subscriptions", {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            Authorization:
              "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
          },
          body: JSON.stringify({
            direction: "ASC",
            page: pageNo,
            properties: ["id"],
            size: pageSize,
          }),
        });
        const res = await response.json();
        setTotalFiles(res.totalElements);
        setSubscribed(res.content);
        setLoader(false);
      } catch (error) {
        console.log(error);
        setLoader(false);
      }
    };
    getSubscribed(pageNo, pageSize);
  }, [pageSize, pageNo]);

  return (
    <div className="wrapped">
      <PageTitle title={"monitoring"} />
      <PerPage pageSize={pageSize} setPageSize={setPageSize} />

      {subscribed.length === 0 ? (
        <h3>
          Об'єкти моніторингу не обрані. Скористайтесь
          <Link className="icons" to="/search">
            Пошуком.
          </Link>
        </h3>
      ) : (
        <div className="d-flex flex-wrap">
          {subscribed.map((person) => {
            return <Card key={person.id} data={person} />;
          })}
        </div>
      )}

      {totalFiles > pageSize && (
        <Pagination
          filesPerPage={pageSize}
          totalFiles={totalFiles}
          paginate={paginate}
        />
      )}
      <Spinner loader={loader} message={"Шукаю збіги"} />
    </div>
  );
};

export default Monitoring;
