import React from "react";

const TableItem = ({ item }) => {
  return (
    <td className="text-center align-middle" title={item}>
      {item}
    </td>
  );
};

export default TableItem;
