import React from "react";

const Phone = ({data}) => {
    const {phone} = data;
    return (
        <div className="card mb-3">
            <p>
                <b className="mr-10">Телефон:</b>
                {phone}
            </p>
        </div>
    );
};

export default Phone;
