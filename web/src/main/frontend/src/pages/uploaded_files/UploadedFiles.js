import React, { useEffect, useState } from "react";
import PageTitle from "../../components/PageTitle";
import Table from "../../components/Table";
import Pagination from "../../components/Pagination";
import authHeader from "../../api/AuthHeader";
import { useDispatch } from "react-redux";
import { setAlertMessageThunk } from "../../store/reducers/AuthReducer";
import * as IoIcons from "react-icons/io";
import ConfirmDeletemodal from "../../components/Modal/ConfirmDeletemodal";

const UploadedFiles = () => {
  const [resp, setResp] = useState([]);
  const [singleFile, setSingleFile] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [filesPerPage] = useState(5);
  const [confirmationRemove, setConfirmationRemove] = useState(null);
  const indexOfLastFile = currentPage * filesPerPage;
  const indexOfFirstFile = indexOfLastFile - filesPerPage;
  const currentFile = resp.slice(indexOfFirstFile, indexOfLastFile);
  const paginate = (pageNumber) => setCurrentPage(pageNumber);
  const dispatch = useDispatch();
  const getFiles = () => {
    fetch("/api/uniPF/getUploaded", { headers: authHeader() })
      .then((response) => response.json())
      .then((data) => setResp(data));
  };

  const deleteAction = (id) => {
    const requestOptions = {
      method: "DELETE",
      headers: authHeader(),
    };
    fetch(`/api/uniPF/delete/${id}`, requestOptions).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk(`Видалено запис ${id}`, "success"));
        getFiles();
        setConfirmationRemove(null);
        setSingleFile([]);
      }
    });
  };

  const enrich = (id) => {
    const requestOptions = {
      method: "POST",
      headers: authHeader(),
    };
    fetch(`/api/uniPF/enricher/${id}`, requestOptions).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk(`Завантажено запис ${id}`, "success"));
        getFiles();
        setConfirmationRemove(null);
      }
    });
  };

  useEffect(() => {
    getFiles();
  }, []);
  const getInfo = (target) => {
    fetch(`/api/uniPF/getUploaded/${target}`, { headers: authHeader() })
      .then((response) => response.json())
      .then((data) => setSingleFile(data));
  };
  return (
    <div className="wrapped">
      <PageTitle title={"uploaded_files"} />

      <div className="sroll-x">
        <table className="table-bordered table w-90">
          <thead>
            <tr>
              <th className="table-header">UUID</th>
              <th className="table-header">ІМ'Я</th>
              <th className="table-header">ЧАС</th>
              <th className="table-header">КІЛЬКІСТЬ РЯДКІВ</th>
              <th className="table-header">КОРОТКИЙ ОПИС</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {currentFile.map((el) => {
              return (
                <tr key={el.uuid}>
                  <td
                    id={el.uuid}
                    onClick={(e) => {
                      getInfo(e.target.id);
                    }}
                  >
                    {el.uuid}
                  </td>

                  <td>{el.userName === "Incognito" ? "test" : el.userName}</td>
                  <td>{el.created}</td>
                  <td>{el.rowCount || "не вказано"}</td>
                  <td>{el.description}</td>
                  <td>
                    <div className="d-flex align-items-center">
                      <button
                        onClick={() => enrich(el.uuid)}
                        className="btn custom-btn"
                      >
                        Імпортувати
                      </button>
                      <span
                        onClick={() => setConfirmationRemove(el.uuid)}
                        style={{ color: "red", fontSize: "30px" }}
                      >
                        <IoIcons.IoMdTrash className="mr-10" />
                      </span>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {confirmationRemove && (
          <ConfirmDeletemodal
            open
            onClose={() => setConfirmationRemove(null)}
            uuid={confirmationRemove}
            deleteAction={deleteAction}
          />
        )}
      </div>
      {resp.length > 5 && (
        <Pagination
          filesPerPage={filesPerPage}
          totalFiles={resp.length}
          paginate={paginate}
        />
      )}

      {singleFile.persons && singleFile.persons.length > 0 && (
        <Table
          data={singleFile.persons}
          err={singleFile.statusListPerson}
          errTag={singleFile.statusListTag}
        />
      )}
    </div>
  );
};

export default UploadedFiles;
