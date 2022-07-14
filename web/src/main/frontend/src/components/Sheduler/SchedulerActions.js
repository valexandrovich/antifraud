import React, { useState } from "react";

const SchedulerActions = ({ row, setEditRow, activateImmediately }) => {
  const [open, setOpen] = useState(false);
  const edit = () => {
    setEditRow(row);
  };
  const activate = (e) => {
    activateImmediately(e);
    setOpen(!open);
  };
  return (
    <td className="text-center align-middle">
      <div onMouseLeave={() => setOpen(false)} className="btn-group">
        <button
          className={open ? "btn custom-btn" : "btn custom-btn-collapse"}
          onClick={edit}
        >
          Редагувати
        </button>
        <button
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
            <button
              id={row.groupName + "/" + row.name}
              onClick={activate}
              name="activate"
              className="btn btn-danger"
            >
              Виконати
            </button>
          </div>
        )}
      </div>
    </td>
  );
};

export default SchedulerActions;
