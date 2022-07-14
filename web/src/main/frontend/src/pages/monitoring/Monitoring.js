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

  const [activeTab, setActiveTab] = useState("fiz");
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
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <PageTitle title={"monitoring"} />
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <ul className="nav nav-tabs">
                <li className="nav-item col-sm-6">
                  <button
                    onClick={() => setActiveTab("fiz")}
                    className={
                      activeTab === "fiz"
                        ? "btn active custom-btn fullWidth text-white"
                        : "nav-link fullWidth .bg-light.bg-gradient bg-opacity-30 p-2 text-success"
                    }
                  >
                    Фізичні особи
                  </button>
                </li>
                <li className="nav-item col-sm-6">
                  <button
                    onClick={() => setActiveTab("ur")}
                    className={
                      activeTab === "ur"
                        ? "btn active custom-btn  fullWidth text-white"
                        : "nav-link fullWidth .bg-light.bg-gradient text-success"
                    }
                  >
                    Юридичні особи
                  </button>
                </li>
              </ul>
              {activeTab === "fiz" && (
                <>
                  <PerPage
                    pageSize={pageSize}
                    setPageSize={setPageSize}
                    setPageNo={setPageNo}
                  />
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
                  <Spinner loader={loader} message={"Шукаю збіги"} />{" "}
                </>
              )}
              {activeTab === "ur" && <h1>ЮРИДИЧНІ ОСОБИ</h1>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Monitoring;
