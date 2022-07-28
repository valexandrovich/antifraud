import React, { useEffect, useRef, useState } from "react";
import { useDispatch } from "react-redux";
import userApi from "../../api/UserApi";
import PerPage from "../PerPage";
import { Link } from "react-router-dom";
import Card from "../YPersonCard/Card";
import Pagination from "../Pagination";
import Spinner from "../Loader";

const Yperson = () => {
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
    userApi.getSubscribed(pageNo, pageSize).then((res) => {
      try {
        setTotalFiles(res ? res.totalElements : null);
        setSubscribed(res.content);
        setLoader(false);
        if (!mountedRef.current) return null;
      } catch (e) {
        console.log(e);
        setLoader(false);
      }
    });
    return () => {
      mountedRef.current = false;
    };
  }, [pageSize, pageNo, dispatch]);

  return (
    <div>
      <PerPage
        pageSize={pageSize}
        setPageSize={setPageSize}
        setPageNo={setPageNo}
      />
      {subscribed?.length === 0 ? (
        <h3>
          Об'єкти моніторингу не обрані. Скористайтесь
          <Link className="icons" to="/search">
            Пошуком.
          </Link>
        </h3>
      ) : (
        <div className="d-flex flex-wrap">
          {subscribed.map((person) => (
            <Card
              key={person.id}
              data={person}
              totalFiles={totalFiles}
              setTotalFiles={setTotalFiles}
            />
          ))}
        </div>
      )}
      <Pagination
        filesPerPage={pageSize}
        totalFiles={totalFiles}
        paginate={paginate}
      />
      <Spinner loader={loader} message={"Шукаю збіги"} />
    </div>
  );
};

export default Yperson;
