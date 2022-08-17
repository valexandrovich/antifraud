import React, { useCallback, useState, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { useDispatch, useSelector } from "react-redux";
import { useHistory } from "react-router";
import authHeader from "../../api/AuthHeader";
import CsvModal from "../Modal/CsvModal";
import Table from "../../common/Table";
import Spinner from "../../common/Loader";
import {
  setAlertMessageThunk,
  setFileID,
} from "../../store/reducers/actions/Actions";

function Dropzone() {
  const [filetoUpload, setFiletoUpload] = useState();
  const [loader, setLoader] = useState(false);
  const [description, setDescription] = useState("");
  const [prevFile, setPrevFile] = useState([]);
  const [csv, setCsv] = useState(null);
  const dispatch = useDispatch();
  const history = useHistory();
  const fileID = useSelector((state) => state.auth.fileID);

  useEffect(() => {
    const resetFile = () => {
      if (fileID != null) {
        fetch(`/api/uniPF/getUploadedPhysical/${fileID}`, {
          headers: authHeader(),
        })
          .then((res) => res.json())
          .then((file) => setPrevFile(file))
          .then(() => setLoader(false));
      }
    };
    resetFile();
  }, [fileID]);

  const [csvoptions, setCsvOptions] = useState({
    delimeter: ";",
    codingType: "UTF-8",
  });
  const updateErrors = async (id, index, value) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    await fetch(
      `/api/uniPF/updatePhysical?id=${id}&index=${index}&value=${value}`,
      requestOptions
    ).then((res) => res.json().then((data) => setPrevFile(data)));
  };
  const fetchData = useCallback(() => {
    const formData = new FormData();
    setLoader(true);
    formData.append("fileName", filetoUpload[0]);
    fetch(
      `/api/uniPF/uploadPhysical?delimiter=${csvoptions.delimeter}&code=${csvoptions.codingType}`,
      {
        headers: authHeader(),
        method: "POST",
        body: formData,
      }
    )
      .then((response) => response.json())
      .then(setCsv(null))
      .then((result) => {
        dispatch(setFileID(result));
        fetch(`/api/uniPF/getUploadedPhysical/${result}`, {
          headers: authHeader(),
        })
          .then((res) => res.json())
          .then((file) => setPrevFile(file))
          .then(() => setLoader(false));
      })
      .catch((error) => {
        setCsv(null);
        setLoader(false);
        console.error(error);
        dispatch(setAlertMessageThunk("Щось пішло не так", "danger"));
        setFiletoUpload(null);
      });
  }, [csvoptions.codingType, csvoptions.delimeter, dispatch, filetoUpload]);
  const handleSubmissionCSV = () => {
    fetchData();
  };

  useEffect(() => {
    const handleSubmission = () => {
      fetchData();
    };
    if (
      filetoUpload &&
      filetoUpload[0].type !== "text/csv" &&
      filetoUpload[0].type !== "text/plain"
    ) {
      handleSubmission();
    }
  }, [filetoUpload, fetchData]);
  const downloadwithDescription = (id, value) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    fetch(
      `/api/uniPF/upload?uuid=${id}&description=${value}`,
      requestOptions
    ).then((res) => {
      if (res.status === 200) {
        dispatch(setAlertMessageThunk("Файл успішно завантажено", "success"));
        history.push("/uploaded_files");
        dispatch(setFileID(null));
        setPrevFile([]);
      }
      if (res.status > 300) {
        console.dir(res);
        dispatch(setAlertMessageThunk("Виникла помилка", "danger"));
      }
    });
  };
  const onDrop = useCallback(
    (acceptedFiles, err) => {
      if (!err[0]?.errors) {
        if (
          acceptedFiles[0].type === "text/csv" ||
          acceptedFiles[0].type === "text/plain"
        ) {
          setCsv(acceptedFiles);
        }
        setFiletoUpload(acceptedFiles);
      } else {
        dispatch(setAlertMessageThunk("Невірний тип файлу", "danger"));
      }
    },
    [dispatch]
  );
  const uploadFileExample = () => {
    fetch("/api/uniPF/downloadPhysicalFile", {
      headers: authHeader(),
    })
      .then(function (response) {
        return response.blob();
      })
      .then((blob) => {
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.href = url;
        link.download = "ExamplePhysicalFile.xlsx";
        link.click();
      });
  };

  const { getRootProps, getInputProps, isDragActive, isDragAccept } =
    useDropzone({
      onDrop,
      multiple: false,
      accept: [".xlsx", ".csv", ".txt"],
    });

  return (
    <>
      <div className="col-sm-6 mt-2">
        <label htmlFor="summary">Короткий опис файлу:</label>
        <input
          name="summary"
          maxLength={255}
          className="form-control "
          type="text"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Короткий опис"
        />
      </div>
      <>
        <div className={"d-flex justify-content-between"}>
          <h3>Завантаження файлу</h3>
          <button
            className={"btn custom-btn d-flex justify-content-end mb-3"}
            onClick={uploadFileExample}
          >
            Приклад файлу
          </button>
        </div>
        {filetoUpload && <p>{filetoUpload[0].name}</p>}
        <div className="drag" {...getRootProps()}>
          <input {...getInputProps()} />
          {isDragAccept && <p>Завантажити</p>}
          {!isDragActive && (
            <p>
              Натисніть для вибору файла з диска чи перетягнить файл сюди...
            </p>
          )}
        </div>
      </>
      <div className="col-sm-12 mt-2">
        <button
          disabled={
            description.trim() === "" ||
            prevFile?.statusListPerson?.length > 0 ||
            prevFile?.statusListTag?.length > 0 ||
            !fileID
          }
          onClick={() =>
            downloadwithDescription(prevFile.persons[0].uuid, description)
          }
          className="btn ml-3 custom-btn mb-2"
        >
          Завантажити
        </button>
      </div>
      {prevFile.persons && prevFile.persons.length > 0 && (
        <Table
          canEdit={true}
          data={prevFile.persons}
          err={prevFile.statusListPerson}
          errTag={prevFile.statusListTag}
          updateErrors={updateErrors}
        />
      )}
      {prevFile?.wrongColumnNameList?.length > 0 && (
        <div>
          <h3 className="text-danger">Невірний формат заголовків:</h3>
          {[prevFile?.wrongColumnNameList].map((el, index) => (
            <h3 key={index} className="text-danger">
              {el}
            </h3>
          ))}
        </div>
      )}
      {csv && (
        <CsvModal
          open
          csvoptions={csvoptions}
          setCsvOptions={setCsvOptions}
          onClose={() => handleSubmissionCSV()}
        />
      )}
      <Spinner loader={loader} message={"Завантажую файл"} />
    </>
  );
}

export default Dropzone;
