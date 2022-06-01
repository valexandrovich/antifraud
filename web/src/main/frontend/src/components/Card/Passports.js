import React from "react";
import {DateObject} from "react-multi-date-picker";

function pad(num, size) {
    const numLength = num.toString().length;
    let zeroCount = size - numLength;
    while (zeroCount > 0) {
        num = "0" + num;
        zeroCount--;
    }
    return num;
}

const Passports = ({data}) => {
    const {
        series,
        number,
        authority,
        issued,
        endDate,
        recordNumber,
        type,
        validity,
    } = data;

    const passpordType = () => {
        if (type === "UA_IDCARD") {
            return (<><p><b className="mr-10">ID картка:</b>{pad(number, 9)}</p>
                <p><b className="mr-10">Запис №:</b>{recordNumber}</p></>);
        }
        if (type === "UA_FOREIGN") {
            return (<><p><b className="mr-10">Закордонний паспорт:</b>{series}{pad(number, 6)}</p>
                <p><b className="mr-10">Запис №:</b>{recordNumber}</p></>);
        }
        if (type === "UA_DOMESTIC") {
            return (<p><b className="mr-10">Паспорт:</b>{series}{pad(number, 6)}</p>);
        }
    };
    return (
        <div className="card mb-3">
            {passpordType()}
            <p>
                <b className="mr-10">Ким виданий:</b>
                {!authority ? "(не вказано)" : authority}
            </p>
            <p>
                <b className="mr-10">Дата
                    видачі:</b>{!issued ? "(невідомо)" : new DateObject(issued).format("DD.MM.YYYY")}
            </p>
            <p>
                <b className="mr-10">Дійсний
                    до:</b>{!endDate ? "(не вказано)" : new DateObject(endDate).format("DD.MM.YYYY")}
            </p>
            <p>
                <b className="mr-10">Статус:</b>{validity ? "Дійсний" : "Недійсний"}
            </p>
        </div>
    );
};

export default Passports;
