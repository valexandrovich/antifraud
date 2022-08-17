import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import * as IoIcons from "react-icons/io";
import { Link } from "react-router-dom";
import userService from "../../api/UserApi";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";

const Card = ({ data, totalFiles, setTotalFiles }) => {
  const { id, name, edrpou, pdv, subscribe, address } = data;
  const [sub, setSub] = useState(subscribe);
  const userRole = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();
  useEffect(() => {
    setSub(subscribe);
  }, [subscribe]);

  const subscribeAction = (id) => {
    try {
      const res = userService.subscribeCompany(id);
      if (res) {
        setSub(!sub);
        totalFiles && setTotalFiles(totalFiles + 1);
        dispatch(
          setAlertMessageThunk(
            `Користувач ${name ? name : ""} доданий до спостереження`,
            "success"
          )
        );
      }
    } catch (e) {
      dispatch(setAlertMessageThunk("Виникла проблема"));
    }
  };
  const unsubscribeAction = (id) => {
    try {
      const res = userService.unsubscribeCompany(id);
      if (res) {
        totalFiles && setTotalFiles(totalFiles - 1);
        setSub(!sub);
        dispatch(
          setAlertMessageThunk(
            `Користувач ${name ? name : ""} видалений зі спостереження`,
            "success"
          )
        );
      }
    } catch (e) {
      dispatch(setAlertMessageThunk("Виникла проблема"));
    }
  };
  const visible = useSelector((state) => state.auth.monitoring);

  return (
    <div className="row">
      <div className="col-md-4">
        <div className="card m-3 search d-flex">
          <div className="card_header d-flex align-items-center justify-content-between ">
            <IoIcons.IoMdBusiness
              style={{ width: 40, height: 40, fontWeight: "bold" }}
            />
            <h6 className="text-center over">{name}</h6>

            {(visible && userRole === "ADVANCED") ||
            (visible && userRole === "ADMIN") ? (
              sub ? (
                <IoIcons.IoMdStar
                  onClick={() => unsubscribeAction(id)}
                  style={{
                    width: 40,
                    height: 40,
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                />
              ) : (
                <IoIcons.IoMdStarOutline
                  onClick={() => subscribeAction(id)}
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
            {edrpou && (
              <div className="d-flex ">
                <b className="mr-10">ЄДРПОУ:</b>
                <p>{edrpou}</p>
              </div>
            )}
            {pdv && (
              <div className="d-flex ">
                <b className="mr-10">Код платника ПДВ:</b>
                <p>{pdv}</p>
              </div>
            )}
            {address && (
              <div className="d-flex ">
                <b className="mr-10">Адреса:</b>
                <p className="over">{address?.address}</p>
              </div>
            )}
          </div>
          <div className="card-footer d-flex justify-content-end pointer">
            <Link className="text-dark" to={`/YCompany/${id}`}>
              Детальніше...
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Card;
