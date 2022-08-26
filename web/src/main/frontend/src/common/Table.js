import React, { useCallback, useEffect, useState } from "react";
import authHeader from "../api/AuthHeader";

const Table = (props) => {
  const [tagsTypes, setTagTypes] = useState([]);
  const fetchTags = useCallback(() => {
    fetch("/api/uniPF/getTagType", { headers: authHeader() })
      .then((response) => response.json())
      .then((res) => {
        setTagTypes(res);
      });
  }, []);
  useEffect(() => {
    fetchTags();
  }, [fetchTags]);
  const getKeys = () => {
    return Object.keys(props.data[0]);
  };

  const getHeader = () => {
    let keys = getKeys();
    return keys.map((key, index) => {
      return <th key={index}>{key.toUpperCase()}</th>;
    });
  };

  const getRowsData = () => {
    const keys = getKeys();
    return props.data.map((row, index) => {
      const errorRow = props.err?.filter(
        (el) => el.personId === row.id || el.companyId === row.id
      );
      return (
        <tr key={row.id}>
          <RenderRow
            t={tagsTypes}
            edit={props.canEdit}
            key={index}
            data={row}
            keys={keys}
            err={errorRow}
            eTag={props.errTag}
            id={row.id}
            updateErrors={props.updateErrors}
          />
        </tr>
      );
    });
  };

  return (
    <div className="sroll-x tableFixHead">
      <table className="table table-striped table-sm">
        <thead>
          <tr>{getHeader()}</tr>
        </thead>
        <tbody>{getRowsData()}</tbody>
      </table>
    </div>
  );
};

export default Table;

const RenderRow = (props) => {
  return props.keys.map((key, index) => {
    const err = props.err?.find((e) => e.columnIndex + 2 === index);
    return err && props.edit === true ? (
      <ErrCell
        updateErrors={props.updateErrors}
        idx={index - 2}
        key={index}
        data={props.data[key]}
        mis={err}
        id={props.id}
        tag={props.t}
      />
    ) : (
      <td key={index}>
        {typeof props.data[key] === "object" && props.data[key] !== null ? (
          <Tag
            updateErrors={props.updateErrors}
            errTag={props.eTag}
            data={props.data[key]}
            editTag={props.edit}
            tag={props.t}
          />
        ) : (
          <AddValue
            tag={props.t}
            canEdit={props.edit}
            updateErrors={props.updateErrors}
            idx={index - 2}
            key={index}
            data={props.data[key]}
            mis={props.data[key]}
            id={props.id}
          />
        )}
      </td>
    );
  });
};

const ErrCell = (props) => {
  const [edit, setEdit] = React.useState(false);
  const [errValue, setErrValue] = React.useState(props.data);
  const { name, id, idx, tag } = props;
  const editMethod = () => {
    if (name !== "mkId") {
      return (
        <>
          {edit ? (
            <td>
              <div className="form-group">
                <label className="text-danger" htmlFor="errorCell">
                  {props.mis.message}
                </label>
                <input
                  name="errorCell"
                  type="text"
                  className="form-control"
                  value={errValue}
                  onChange={(e) => setErrValue(e.target.value)}
                />
              </div>
              <div className="d-flex justify-content-between mt-1">
                <button
                  onClick={() => props.updateErrors(id, idx, errValue)}
                  type="button"
                  className="btn custom-btn  btn-default"
                >
                  Змінити
                </button>
                <button
                  onClick={() => setEdit(!edit)}
                  type="button"
                  className="btn btn-danger btn-default"
                >
                  Закрити
                </button>
              </div>
            </td>
          ) : (
            <td
              onClick={() => setEdit(true)}
              className="text-danger border border-danger p-0 pointer"
            >
              <span className="table-header">{name?.replace("mk", "")}</span>
              <span>{props.data} </span>
            </td>
          )}
        </>
      );
    } else {
      return (
        <>
          {edit ? (
            <td>
              <div className="form-group">
                <label className="text-danger" htmlFor="errorCell">
                  {props.mis.message}
                </label>
                <select
                  className="form-select"
                  name="errorCell"
                  value={errValue}
                  onChange={(e) => setErrValue(e.target.value)}
                >
                  {tag &&
                    tag.map((el) => (
                      <option key={el.code} value={el.code}>
                        {el.code}
                      </option>
                    ))}
                </select>
              </div>
              <div className="d-flex justify-content-between mt-1">
                <button
                  onClick={() => props.updateErrors(id, idx, errValue)}
                  type="button"
                  className="btn custom-btn  btn-default"
                >
                  Змінити
                </button>
                <button
                  onClick={() => setEdit(!edit)}
                  type="button"
                  className="btn btn-danger btn-default"
                >
                  Закрити
                </button>
              </div>
            </td>
          ) : (
            <td
              onClick={() => setEdit(true)}
              className="text-danger border border-danger p-0 pointer"
            >
              <span className="table-header">{name?.replace("mk", "")}</span>
              <span>{props.data} </span>
            </td>
          )}
        </>
      );
    }
  };
  return editMethod();
};

const Tag = ({ data, errTag, updateErrors, editTag, tag }) => {
  return (
    <>
      {data.map((el) => {
        const err = errTag?.filter((e) => e.tagId === el.id);
        return (
          <div key={el.id} className="tags d-flex border-bottom">
            {Object.entries(el)
              .slice(1)
              .map(([k, v], index) => {
                const errCell = err?.find((e) => e.columnIndex === index);
                if (errCell?.columnIndex === index) {
                  return (
                    <>
                      {
                        <ErrCell
                          updateErrors={updateErrors}
                          key={index}
                          idx={index}
                          id={el.id}
                          data={v}
                          mis={errCell.columnIndex === index && errCell}
                          name={k}
                          tag={tag}
                        />
                      }
                    </>
                  );
                }
                return (
                  <div
                    className="d-flex flex-column align-items-center"
                    key={index}
                  >
                    <AddValue
                      canEdit={editTag}
                      updateErrors={updateErrors}
                      key={index}
                      idx={index}
                      id={el.id}
                      data={v}
                      mis={v}
                      name={k}
                    />
                  </div>
                );
              })}
          </div>
        );
      })}
    </>
  );
};

const AddValue = (props) => {
  const [edit, setEdit] = React.useState(false);
  const [errValue, setErrValue] = React.useState(props.data);
  const { name, id, idx, canEdit } = props;
  return (
    <>
      {edit ? (
        <td>
          <div className="form-group">
            <label htmlFor="errorCell">{props.mis?.message}</label>
            <input
              disabled={idx === -2 || idx === -1}
              name="errorCell"
              type="text"
              className="form-control"
              value={errValue ? errValue : ""}
              onChange={(e) => setErrValue(e.target.value)}
            />
          </div>
          <div className="d-flex justify-content-between mt-1">
            <button
              disabled={idx === -2 || idx === -1}
              onClick={() => {
                props.updateErrors(id, idx, errValue);
                setEdit(false);
              }}
              type="button"
              className="btn custom-btn  btn-default"
            >
              Змінити
            </button>
            <button
              onClick={() => setEdit(!edit)}
              type="button"
              className="btn btn-danger btn-default"
            >
              Закрити
            </button>
          </div>
        </td>
      ) : (
        <>
          {canEdit ? (
            <td onDoubleClick={() => setEdit(!edit)} className="p-0 editable">
              <span className="table-header">{name?.replace("mk", "")}</span>
              <span>{props.data} </span>
            </td>
          ) : (
            <td className="p-0 editable">
              <span className="table-header">{name?.replace("mk", "")}</span>
              <span>{props.data} </span>
            </td>
          )}
        </>
      )}
    </>
  );
};
