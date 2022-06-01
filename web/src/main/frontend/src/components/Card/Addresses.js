import React from "react";

const Addresses = ({data}) => {
    const {address} = data;
    return (
        <div className="card mb-3">
            <p>
                <b className="mr-10">Адреса:</b>
                {address}
            </p>
        </div>
    );
};

export default Addresses;
