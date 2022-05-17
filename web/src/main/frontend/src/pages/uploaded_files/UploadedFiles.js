import React, { useEffect, useState } from "react";
import PageTitle from "../../components/PageTitle";
import Table from "../../components/Table";
import Pagination from "../../components/Pagination";
import authHeader from "../../api/AuthHeader";

const UploadedFiles = () => {
  const [resp, setResp] = useState([]);
  const [singleFile, setSingleFile] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [filesPerPage] = useState(5);

  const indexOfLastFile = currentPage * filesPerPage;
  const indexOfFirstFile = indexOfLastFile - filesPerPage;
  const currentFile = resp.slice(indexOfFirstFile, indexOfLastFile);
  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  useEffect(() => {
    fetch("/api/uniPF/getUploaded", { headers: authHeader() })
      .then((response) => response.json())
      .then((data) => setResp(data));
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
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <Pagination
        filesPerPage={filesPerPage}
        totalFiles={resp.length}
        paginate={paginate}
      />
      {singleFile.persons && singleFile.persons.length > 0 && (
        <Table data={singleFile.persons} err={singleFile.cellStatuses} />
      )}
    </div>
  );
};

export default UploadedFiles;
