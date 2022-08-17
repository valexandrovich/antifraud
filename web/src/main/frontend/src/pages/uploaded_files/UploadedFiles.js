import React, { useCallback, useEffect, useRef, useState } from "react";
import PageTitle from "../../common/PageTitle";
import Table from "../../common/Table";
import Pagination from "../../common/Pagination";
import authHeader from "../../api/AuthHeader";
import { useDispatch } from "react-redux";
import ConfirmDeletemodal from "../../components/Modal/ConfirmDeletemodal";
import UploadFilesActions from "./UploadFilesActions";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";
import { DateObject } from "react-multi-date-picker";
import * as IoIcons from "react-icons/io";

const UploadedFiles = () => {
  const [resp, setResp] = useState([]);
  const [singleFile, setSingleFile] = useState([]);

  const [currentPage, setCurrentPage] = useState(1);
  const [filesPerPage] = useState(15);
  const [confirmationRemove, setConfirmationRemove] = useState(null);
  const indexOfLastFile = currentPage * filesPerPage;
  const indexOfFirstFile = indexOfLastFile - filesPerPage;
  const currentFile = resp.slice(indexOfFirstFile, indexOfLastFile);
  const paginate = useCallback((pageNumber) => setCurrentPage(pageNumber), []);
  const dispatch = useDispatch();
  const mountedRef = useRef(true);
  const bottomRef = useRef(null);

  useEffect(() => {
    // eslint-disable-next-line no-unused-expressions
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [singleFile]);

  const getFiles = useCallback(() => {
    fetch("/api/uniPF/getUploaded", { headers: authHeader() })
      .then((response) => response.json())
      .then((data) => {
        if (!mountedRef.current) return null;
        setResp(data);
      });
  }, []);
  useEffect(() => {
    setSingleFile([]);
  }, [currentPage]);
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
    return () => {
      mountedRef.current = false;
    };
  }, [getFiles]);
  const getInfo = (target, fileType) => {
    if (fileType === "PHYSICAL") {
      fetch(`/api/uniPF/getUploadedPhysical/${target}`, {
        headers: authHeader(),
      })
        .then((response) => response.json())
        .then((data) => setSingleFile(data));
    } else {
      fetch(`/api/uniPF/getUploadedJuridical/${target}`, {
        headers: authHeader(),
      })
        .then((response) => response.json())
        .then((data) => setSingleFile(data));
    }
  };
  return (
    <div className="wrapped">
      <PageTitle title={"uploaded_files"} />

      <div className="scroll tableFixHead">
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
            {currentFile
              .sort((a, b) => {
                return a.started < b.started ? 1 : -1;
              })
              .map((el) => {
                return (
                  <tr className={"align-middle action_btn"} key={el.uuid}>
                    <td
                      className={"action_btn_clicked"}
                      id={el.uuid}
                      onClick={() => {
                        getInfo(el.uuid, el.type?.name);
                      }}
                    >
                      {el.uuid}
                      {el.type?.name === "PHYSICAL" || el.type?.name == null ? (
                        <IoIcons.IoMdPerson
                          style={{
                            width: 20,
                            height: 20,
                            fontWeight: "bold",
                            marginLeft: 10,
                          }}
                        />
                      ) : (
                        <IoIcons.IoMdBusiness
                          style={{
                            width: 20,
                            height: 20,
                            fontWeight: "bold",
                            marginLeft: 10,
                          }}
                        />
                      )}
                    </td>
                    <td>{el.userName}</td>

                    <td>
                      {new DateObject(el.created.split("T").join()).format(
                        "DD.MM.YYYY hh:mm:ss"
                      )}
                    </td>
                    <td>{el.rowCount || "не вказано"}</td>
                    <td>{el.description}</td>
                    <UploadFilesActions
                      type={el.type?.name}
                      enrich={enrich}
                      el={el.uuid}
                      remove={() => setConfirmationRemove(el.uuid)}
                      info={(e) => {
                        getInfo(e, el.type?.name);
                      }}
                    />
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
      {resp.length > 15 && (
        <Pagination
          margin={-65}
          filesPerPage={filesPerPage}
          totalFiles={resp.length}
          paginate={paginate}
        />
      )}

      {singleFile.persons && singleFile.persons.length > 0 && (
        <>
          <Table canEdit={false} data={singleFile.persons} />
          <div ref={bottomRef}></div>
        </>
      )}
      {singleFile.companies && singleFile.companies.length > 0 && (
        <>
          <Table data={singleFile.companies} />
          <div ref={bottomRef}></div>
        </>
      )}
    </div>
  );
};

export default UploadedFiles;
