import React, { useCallback, useState, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { useDispatch } from "react-redux";
import { useHistory } from "react-router";
import authHeader from "../api/AuthHeader";
import { setAlertMessageThunk } from "../store/reducers/AuthReducer";
import CsvModal from '../components/Modal/СsvModal';
import Table from "./Table";

function Dropzone() {
  const [filetoUpload, setFiletoUpload] = useState();
  const [description, setDescription] = useState("");
  const [prevFile, setPrevFile] = useState([]);
  const [csv, setCsv] = useState(null);
  const dispatch = useDispatch();
  const history = useHistory();
  const [csvoptions, setCsvOptions] = useState({
    delimeter: ";",
    codingType: "UTF-8"
  });
  const updateErrors = async (id, index, value) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    await fetch(`/api/uniPF/update?id=${id}&index=${index}&value=${value}`, requestOptions).then(res => res.json().then(data => setPrevFile(data)));
  };

  const fetchData = useCallback(() => {
    const formData = new FormData();
    formData.append("fileName", filetoUpload[0]);
    fetch(`/api/uniPF/upload?delimiter=${csvoptions.delimeter}&code=${csvoptions.codingType}`, {
      headers: authHeader(),
      method: "POST",
      body: formData,
    })
      .then((response) => response.json())
      .then((result) => {
        fetch(`/api/uniPF/getUploaded/${result}`, { headers: authHeader() })
          .then((res) => res.json())
          .then((file) => setPrevFile(file))
          .then(setCsv(null));
      })
      .catch((error) => {
        console.error(error);
        dispatch(setAlertMessageThunk("Щось пішло не так", "danger"));
        setCsv(null);
      });
  }, [csvoptions.codingType, csvoptions.delimeter, dispatch, filetoUpload]);
  const handleSubmissionCSV = () => {
    fetchData();
  };

  useEffect(() => {
    const handleSubmission = () => {
      fetchData();
    };
    if (filetoUpload && filetoUpload[0].type !== "text/csv"
      && filetoUpload[0].type !== "text/plain") {
      handleSubmission();
    }
  }, [filetoUpload, fetchData]);
  const downloadwithDescription = (id, value) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    fetch(`/api/uniPF/upload?uuid=${id}&description=${value}`, requestOptions)
      .then(() =>
        dispatch(setAlertMessageThunk("Файл успішно завантажено", "success"))
      );
    history.push("/uploaded_files");
  };
  const onDrop = useCallback(
    (acceptedFiles, err) => {
      if (!err[0]?.errors) {
        if (acceptedFiles[0].type === "text/csv" || acceptedFiles[0].type === "text/plain") {
          setCsv(acceptedFiles);
        }
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
      accept: [".xlsx", ".csv", ".txt"],
    });

  return (
    <>
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
      {description.length >= 1 &&
        <>
          <h3>Завантаження файлу</h3>
          {filetoUpload && <p>{filetoUpload[0].name}</p>}
          <div className="drag" {...getRootProps()}>
            <input {...getInputProps()} />
            {isDragAccept && <p>Завантажити</p>}
            {!isDragActive && <p>Завантажте файл ...</p>}
          </div>
        </>}
      <div className="col-sm-12 mt-2">
        <button
          disabled={description === "" || (prevFile?.statusListPerson?.length > 0 && prevFile?.statusListTag?.length > 0) || !filetoUpload}
          onClick={() =>
            downloadwithDescription(prevFile.persons[0].uuid, description)
          }
          className="btn ml-3 custom-btn mb-2"
        >
          Завантажити
        </button>
      </div>
      {prevFile.persons && prevFile.persons.length > 0 &&
        <Table data={prevFile.persons} err={prevFile.statusListPerson} errTag={prevFile.statusListTag} updateErrors={updateErrors} />
      }
      {[prevFile?.wrongColumnNameList].map((el, index) => <h3 key={index} className="text-danger">{el}</h3>)}
      {csv && <CsvModal open csvoptions={csvoptions}
        setCsvOptions={setCsvOptions} onClose={() => handleSubmissionCSV()} />}
    </>

  );
}

export default Dropzone;
