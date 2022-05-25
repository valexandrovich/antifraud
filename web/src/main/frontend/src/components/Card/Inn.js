import React from "react";

const Inn = ({ data }) => {
  const { inn } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">ІПН:</b> {inn}
      </p>
    </div>
  );
};

export default Inn;
