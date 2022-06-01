import React from "react";

const AltPerson = ({data}) => {
    const {lastName, firstName, patName, language} = data;
    return (
        <div className="card mb-3">
            <p>
                <b className="mr-10">ПІБ:</b>
                {lastName}
                {""} {firstName}
                {""} {patName}
            </p>
            {language}
        </div>
    );
};

export default AltPerson;
