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

  useEffect(() => {
    fetch("/getUploaded")
      .then((response) => response.json())
      .then((data) => setResp(data.uuid));
  }, []);
  const getInfo = (target) => {
    fetch(`/getUploaded/${target}`)
      .then((response) => response.json())
      .then((data) => setSingleFile(data.uuid));
  };
  return (
    <div className="wrapped">
      <PageTitle title={"uploaded_files"} />
      <div className="card">
        <ul className="list-group list-group-flush">
          {currentFile.map((el) => (
            <li
              className="list-group-item"
              id={el.uuid}
              key={el.uuid}
              onClick={(e) => {
                getInfo(e.target.id);
              }}
            >
              <h3>{el.description ? el.description : el.uuid}</h3>{" "}
              <h4>{el.created}</h4>
            </li>
          ))}
        </ul>
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
