import React, { useState } from "react";

const UploadFilesActions = ({ enrich, remove, el, info }) => {
  const [open, setOpen] = useState(false);
  const enrichment = () => {
    setOpen(false);
    enrich(el);
  };

  const removeEl = () => {
    setOpen(!open);
    remove(el.uuid);
  };

  const getInfo = () => {
    setOpen(!open);
    info(el);
  };

  return (
    <td className={"text-center align-middle position-relative"}>
      <div onMouseLeave={() => setOpen(false)} className={"btn-group"}>
        <button
          className={open ? "btn custom-btn" : "btn custom-btn-collapse"}
          onClick={enrichment}
        >
          Імпортувати
        </button>
        <button
          id={el.uuid}
          className={
            open
              ? "btn custom-btn dropdown-toggle"
              : "dropdown-toggle btn custom-btn-collapse"
          }
          onClick={() => setOpen(!open)}
        >
          <span className="sr-only"></span>
        </button>
        {open && (
          <div className={"dropdown-element"}>
            <button className="btn btn-danger" onClick={removeEl}>
              Видалити
            </button>
            <button id={el.uuid} className="btn custom-btn" onClick={getInfo}>
              Переглянути
            </button>
          </div>
        )}
      </div>
    </td>
  );
};

export default UploadFilesActions;
