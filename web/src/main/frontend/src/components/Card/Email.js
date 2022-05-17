import React from "react";

const Email = ({ data }) => {
  const { email } = data;
  return (
    <div className="card mb-3">
      <p>
        <b className="mr-10">Email:</b>
        {email}
      </p>
    </div>
  );
};

export default Email;
