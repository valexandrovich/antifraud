import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import Card from "../../components/Card/Card";
import PageTitle from "../../components/PageTitle";
import Pagination from "../../components/Pagination";
import PerPage from "../../components/PerPage";
import Spinner from "../../components/Loader";
import userApi from "../../api/UserApi";
import { useDispatch } from "react-redux";

const Monitoring = () => {
  const [subscribed, setSubscribed] = useState([]);
  const [pageSize, setPageSize] = useState(6);
  const [pageNo, setPageNo] = useState(0);
  const [totalFiles, setTotalFiles] = useState();
  const [loader, setLoader] = useState(false);
  const dispatch = useDispatch();
  const paginate = (pageNumber) => setPageNo(pageNumber - 1);
  const mountedRef = useRef(true);
  useEffect(() => {
    setLoader(true);
    userApi.getSubscribed(pageNo, pageSize, dispatch).then((res) => {
      try {
        setTotalFiles(res.totalElements);
        setSubscribed(res.content);
        setLoader(false);
        if (!mountedRef.current) return null;
      } catch (e) {
        setLoader(false);
      }
    });
    return () => {
      mountedRef.current = false;
    };
  }, [pageSize, pageNo, dispatch]);

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
