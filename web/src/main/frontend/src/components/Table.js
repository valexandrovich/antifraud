import React from "react";

const Table = (props) => {
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
    const items = props.data;
    const keys = getKeys();
    return items.map((row, index) => {
      return (
        <tr key={index}>
          <RenderRow key={index} data={row} keys={keys} />
        </tr>
      );
    });
  };
  return (
    <div className="sroll-x">
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
    return <td key={index}>{props.data[key]}</td>;
  });
};
