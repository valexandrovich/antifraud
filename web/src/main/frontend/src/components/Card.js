import React, { useState } from "react";
import * as IoIcons from "react-icons/io";
import { useDispatch } from "react-redux";
import { Link } from "react-router-dom";
import authHeader from "../api/AuthHeader";

import { setAlertMessageThunk } from "../store/reducers/AuthReducer";

const Card = ({ data }) => {
  const {
    id,
    lastName,
    firstName,
    patName,
    inns,
    birthdate,
    addresses,
    subscribe,
  } = data;
  const [sub, setSub] = useState(subscribe);

  const dispatch = useDispatch();
  const subscribeAction = (i) => {
    const requestOptions = {
      method: "PUT",
      headers: authHeader(),
    };
    fetch(`/api/uniPF/unsubscribe/${i}`, requestOptions)
      .then((res) => {
        if (res.status === 200) {
          setSub(!sub);
          dispatch(
            setAlertMessageThunk(
              `Користувач ${firstName} ${lastName} видалений зі спостереження`,

              "success"
            )
          );
        }
      })
      .then((res) => console.log(res));
  };

  const unsubscribeAction = (i) => {
    const requestOptions = {
      method: "PUT",

      headers: authHeader(),
    };
    fetch(`/api/uniPF/subscribe/${i}`, requestOptions)
      .then((res) => {
        if (res.status === 200) {
          setSub(!sub);
          dispatch(
            setAlertMessageThunk(
              `Користувач ${firstName} ${lastName} доданий до спостереження`,
              "success"
            )
          );
        }
      })
      .then((res) => console.log(res));
  };

  return (
    <>
      <div className="card m-3 search d-flex">
        <div className="card_header d-flex align-items-center justify-content-between ">
          <IoIcons.IoMdPerson
            style={{ width: 40, height: 40, fontWeight: "bold" }}
          />
          <h6 className="text-center">
            {lastName} {firstName} {patName}
          </h6>
          {sub ? (
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
          )}
        </div>
        <hr />
        <div className="card-body">
          <div className="d-flex flex-md-column">
            <b className="mr-10">Дата народження:</b>
            <p>{birthdate}</p>
          </div>
          <div className="d-flex">
            <b className="mr-10">Инн:</b>
            <p>{inns.length > 0 ? inns[0].inn : ""}</p>
          </div>
          <div className="d-flex">
            <b className="mr-10">Адреса:</b>
            <p>{addresses.length > 0 ? addresses[0].address : ""}</p>
          </div>
        </div>
        <div className="card-footer d-flex justify-content-end pointer">
          <Link className="text-dark" to={`/card/${id}`}>
            Детальніше...
          </Link>
        </div>
      </div>
    </>
  );
};

export default Card;
