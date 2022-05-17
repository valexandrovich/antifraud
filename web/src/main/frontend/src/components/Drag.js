import React, { useCallback, useState, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { useDispatch } from "react-redux";
import { useHistory } from "react-router";
import authHeader from "../api/AuthHeader";
import { setAlertMessageThunk } from "../store/reducers/AuthReducer";
import Table from "./Table";

function Dropzone() {
  const [filetoUpload, setFiletoUpload] = useState("");
  const [description, setDescription] = useState("");
  const [prevFile, setPrevFile] = useState([]);
  const dispatch = useDispatch();
  const history = useHistory();
  useEffect(() => {
    const handleSubmission = () => {
      const formData = new FormData();
      formData.append("fileName", filetoUpload[0]);
      fetch("/api/uniPF/upload", {
        headers: authHeader(),
        method: "POST",
        body: formData,
      })
        .then((response) => response.json())
        .then((result) => {
          fetch(`/api/uniPF/getUploaded/${result}`, { headers: authHeader() })
            .then((res) => res.json())
            .then((file) => setPrevFile(file));
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
      headers: authHeader(),
    };
    fetch(`/api/uniPF/upload?uuid=${id}&description=${value}`, requestOptions)
      .then(() =>
        dispatch(setAlertMessageThunk("Файл успішно завантажено", "success"))
      )
      .then(history.push("/uploaded_files"));
  };
  const onDrop = useCallback(
    (acceptedFiles, err) => {
      if (!err[0]?.errors) {
        setFiletoUpload(acceptedFiles);
      } else {
        dispatch(setAlertMessageThunk("Невірний тип файлу", "danger"));
      }
    },
    [dispatch]
  );

  const { getRootProps, getInputProps, isDragActive, isDragAccept } =
    useDropzone({
      onDrop,
      multiple: false,
      accept: ".xlsx",
    });

  return (
    <>
      <h3>Завантаження файлу</h3>
      {filetoUpload && <p>{filetoUpload[0].name}</p>}
      <div className="drag" {...getRootProps()}>
        <input {...getInputProps()} />
        {isDragAccept && <p>Завантажити</p>}
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
          disabled={description === "" || !filetoUpload}
          onClick={() =>
            downloadwithDescription(prevFile.persons[0].uuid, description)
          }
          className="btn ml-3 custom-btn"
        >
          Завантажити
        </button>
      </div>
      {prevFile.persons && prevFile.persons.length > 0 && (
        <Table data={prevFile.persons} err={prevFile.cellStatuses} />
      )}
    </>
  );
}

export default Dropzone;
