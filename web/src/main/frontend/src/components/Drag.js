import React, { useCallback, useState, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { useDispatch } from "react-redux";
import { setAlertMessageThunk } from "../store/reducers/AuthReducer";
import Table from "./Table";

function Dropzone() {
  const [filetoUpload, setFiletoUpload] = useState("");
  const [description, setDescription] = useState("");
  const [prevFile, setPrevFile] = useState(null);
  const dispatch = useDispatch();
  const [token, setToken] = useState("");
  useEffect(() => {
    const authToken = localStorage.getItem("user");
    setToken(authToken.replace(/"/g, ""));
  }, [token]);

  useEffect(() => {
    const handleSubmission = () => {
      const formData = new FormData();
      formData.append("fileName", filetoUpload[0]);
      fetch("/upload", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        method: "POST",

        body: formData,
      })
          .then((response) => response.json())
          .then((result) => {
            fetch(`/getUploaded/${result.principal}`)
                .then((res) => res.json())
                .then((file) => setPrevFile(file.principal));
          })
          .catch((error) => {
            console.error("Error:", error);
          });
    };
    if (filetoUpload) {
      handleSubmission();
    }
  }, [filetoUpload]);
  const downloadwithDescription = (id, value) => {
    const requestOptions = {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: "Fetch PUT Request Example" }),
    };
    fetch(`/upload?uuid=${id}&description=${value}`, requestOptions)
        .then((response) => response.json())
        .then(() =>
            dispatch(setAlertMessageThunk("Файл успішно завантажено", "success"))
        )
        .then(() => setFiletoUpload(""))
        .then(() => setDescription(""));
  };
  const onDrop = useCallback((acceptedFiles) => {
    setFiletoUpload(acceptedFiles);
  }, []);

  const {
    getRootProps,
    getInputProps,
    isDragActive,
    isDragAccept,
    isDragReject,
  } = useDropzone({
    onDrop,
    multiple: false,
    accept: ".xlsx, .xls, .csv",
  });

  return (
      <>
        <h3>Завантаження файлу</h3>
        {filetoUpload && <p>{filetoUpload[0].name}</p>}
        <div className="drag" {...getRootProps()}>
          <input {...getInputProps()} />
          {isDragAccept && <p>Завантажити</p>}
          {isDragReject && <p>Невірний тип файлу</p>}
          {!isDragActive && <p>Завантажте файл ...</p>}
        </div>
        <div className="col-sm-12 mt-2">
          <label htmlFor="summary">Короткий опис файлу:</label>
          <input
              name="summary"
              className="form-control "
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Короткий опис"
          />
        </div>
        <div className="col-sm-12 mt-2">
          <button
              onClick={() => downloadwithDescription(prevFile[0].uuid, description)}
              className="btn btn-primary ml-3"
          >
            Завантажити
          </button>
        </div>
        {prevFile && prevFile.length > 0 && <Table data={prevFile} />}
      </>
  );
}

export default Dropzone;