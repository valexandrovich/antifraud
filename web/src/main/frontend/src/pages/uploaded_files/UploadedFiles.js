import React, { useEffect, useState } from "react";
import PageTitle from "../../components/PageTitle";
import Table from "../../components/Table";
import Pagination from "../../components/Pagination";

const UploadedFiles = () => {
  const [resp, setResp] = useState([]);
  const [singleFile, setSingleFile] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [filesPerPage] = useState(5);

  const indexOfLastFile = currentPage * filesPerPage;
  const indexOfFirstFile = indexOfLastFile - filesPerPage;
  const currentFile = resp.slice(indexOfFirstFile, indexOfLastFile);
  const paginate = (pageNumber) => setCurrentPage(pageNumber);
  console.log(singleFile);
  useEffect(() => {
    fetch("/getUploaded")
      .then((response) => response.json())
      .then((data) => setResp(data.principal));
  }, []);
  const getInfo = (target) => {
    fetch(`/getUploaded/${target}`)
      .then((response) => response.json())
      .then((data) => setSingleFile(data.principal));
  };
  return (
    <div className="wrapped">
      <PageTitle title={"uploaded_files"} />
      <div className="card">
        <table className="table-bordered table-sm">
          <thead>
            <tr>
              <th>UUID</th>
              <th>ІМ'Я</th>
              <th>ЧАС</th>
              <th>КІЛЬКІСТЬ РЯДКІВ</th>
              <th>КОРОТКИЙ ОПИС</th>
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

                  <td>{el.userName || "USER"}</td>
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
      {singleFile && singleFile.length > 0 && <Table data={singleFile} />}
    </div>
  );
};

export default UploadedFiles;
