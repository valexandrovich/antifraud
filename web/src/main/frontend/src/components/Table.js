import React, { useRef } from "react";

const Table = (props) => {
  const onWheel = (e) => {
    e.preventDefault();
    const container = scrollRef.current;
    const containerScrollPosition = scrollRef.current.scrollLeft;

    container.scrollTo({
      top: containerScrollPosition,
      left: containerScrollPosition + e.deltaY,
    });
  };

  const scrollRef = useRef(null);

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
      const errorRow = props.err.filter((el) => el.personId === row.id);
      return (
        <tr key={row.id}>
          <RenderRow key={index} data={row} keys={keys} err={errorRow} />
        </tr>
      );
    });
  };
  return (
    <div ref={scrollRef} onWheel={onWheel} className="sroll-x">
      <table className="table table-striped table-bordered table-sm">
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
    const err = props.err.find((e) => e.columnIndex + 2 === index);
    return err ? (
      <ErrCell key={index} data={props.data[key]} mis={err} />
    ) : (
      <td key={index}>{props.data[key]}</td>
    );
  });
};

const ErrCell = (props) => {
  const [edit, setEdit] = React.useState(false);
  const [errValue, setErrValue] = React.useState(props.data);
  const toggleEdit = () => {
    setEdit(!edit);
  };
  return (
    <>
      {edit ? (
        <td>
          <div
            style={{ position: "relative", width: "300px" }}
            className="form-group"
          >
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
          <button type="button" className="btn-success btn-default me-3">
            Змінити
          </button>
          <button
            onClick={toggleEdit}
            type="button"
            className="btn-danger btn-default"
          >
            Закрити
          </button>
        </td>
      ) : (
        <td className="text-danger" onDoubleClick={toggleEdit}>
          {props.data}
        </td>
      )}
    </>
  );
};
