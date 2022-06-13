import React, { useState } from "react";
import * as IoIcons from "react-icons/io";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import authHeader from "../../api/AuthHeader";
import { DateObject } from "react-multi-date-picker";
import { setAlertMessageThunk } from "../../store/reducers/AuthReducer";

export function pad(num, size) {
  const numLength = num.toString().length;
  let zeroCount = size - numLength;
  while (zeroCount > 0) {
    num = "0" + num;
    zeroCount--;
  }
  return num;
}

export function formatPassport(passports) {
  if (passports.length > 0) {
    if (passports[0].type === "UA_IDCARD") {
      return pad(passports[0].number, 9);
    } else {
      return `${passports[0].series}${pad(passports[0].number, 6)}`;
    }
  }
  return "";
}

export function formatInn(inns) {
  if (inns.length > 0) {
    if (inns[0].inn.toString().length < 10) {
      return pad(inns[0].inn, 10);
    } else {
      return inns[0].inn;
    }
  }
  return "";
}

export function sourceName(source) {
  return source.length === 1 ? "джерело" : "джерела";
}

const Card = ({ data }) => {
  const {
    id,
    lastName,
    firstName,
    patName,
    inns,
    passports,
    birthdate,
    addresses,
    subscribe,
  } = data;
  const [sub, setSub] = useState(subscribe);
  const role = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();
  const subscribeAction = (i) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    fetch(`/api/user/unsubscribe/${i}`, requestOptions).then((res) => {
      if (res.status === 200) {
        setSub(!sub);
        dispatch(
          setAlertMessageThunk(
            `Користувач ${firstName ? firstName : ""} ${
              lastName ? lastName : ""
            } видалений зі спостереження`,
            "success"
          )
        );
      }
    });
  };

  const unsubscribeAction = (i) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    fetch(`/api/user/subscribe/${i}`, requestOptions).then((res) => {
      if (res.status === 200) {
        setSub(!sub);
        dispatch(
          setAlertMessageThunk(
            `Користувач ${firstName ? firstName : ""} ${
              lastName ? lastName : ""
            } доданий до спостереження`,
            "success"
          )
        );
      }
    });
  };

  return (
    <div className="row">
      <div className="col-md-4">
        <div className="card m-3 search d-flex">
          <div className="card_header d-flex align-items-center justify-content-between ">
            <IoIcons.IoMdPerson
              style={{ width: 40, height: 40, fontWeight: "bold" }}
            />
            <h6 className="text-center">
              {lastName} {firstName} {patName}
            </h6>

            {role === "ADVANCED" ? (
              sub ? (
                <IoIcons.IoMdStar
                  onClick={() => subscribeAction(id)}
                  style={{
                    width: 40,
                    height: 40,
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                />
              ) : (
                <IoIcons.IoMdStarOutline
                  onClick={() => unsubscribeAction(id)}
                  style={{
                    width: 40,
                    height: 40,
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                />
              )
            ) : (
              <span />
            )}
          </div>
          <hr />
          <div className="card-body">
            <div className="d-flex ">
              <b className="mr-10">Дата народження:</b>
              <p>{new DateObject(birthdate).format("DD.MM.YYYY")}</p>
            </div>
            <div className="d-flex ">
              <b className="mr-10">Паспорт:</b>
              <p>{formatPassport(passports)}</p>

              <span className="ml-10">
                {passports.length > 0
                  ? `(${passports[0]?.importSources.length} ${sourceName(
                      passports[0]?.importSources
                    )})`
                  : ""}
              </span>
            </div>
            <div className="d-flex">
              <b className="mr-10">ІПН:</b>
              <p>{formatInn(inns)} </p>
              <span className="ml-10">
                {inns.length > 0
                  ? `(${inns[0].importSources.length} ${sourceName(
                      inns[0].importSources
                    )})`
                  : ""}
              </span>
            </div>
            <div className="d-flex">
              <b className="mr-10">Адреса:</b>
              <p>{addresses.length > 0 ? addresses[0].address : ""}</p>
              <span className="ml-10">
                {addresses.length > 0
                  ? `(${addresses[0].importSources.length} ${sourceName(
                      addresses[0].importSources
                    )})`
                  : ""}
              </span>
            </div>
          </div>
          <div className="card-footer d-flex justify-content-end pointer">
            <Link className="text-dark" to={`/card/${id}`}>
              Детальніше...
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Card;
