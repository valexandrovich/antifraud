import React, { useEffect, useState } from "react";
import * as IoIcons from "react-icons/io";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { DateObject } from "react-multi-date-picker";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";
import userService from "../../api/UserApi";
import authHeader from "../../api/AuthHeader";

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
  if (passports && passports.length > 0) {
    if (passports[0].type === "UA_IDCARD") {
      return pad(passports[0].number, 9);
    } else {
      return `${passports[0].series}${pad(passports[0].number, 6)}`;
    }
  }
  return "";
}

function singlePassport(passport) {
  if (passport) {
    if (passport.type === "UA_IDCARD") {
      return pad(passport.number, 9);
    } else {
      return `${passport.series}${pad(passport.number, 6)}`;
    }
  }
  return "";
}

export function formatInn(inns) {
  if (inns && inns.length > 0) {
    if (inns[0].inn.toString().length < 10) {
      return pad(inns[0].inn, 10);
    } else {
      return inns[0].inn;
    }
  }
  return "";
}

export function singleInn(inn) {
  if (inn) {
    if (inn.inn.toString().length < 10) {
      return pad(inn.inn, 10);
    } else {
      return inn.inn;
    }
  }
  return "";
}

export function sourceName(source) {
  return source.length === 1 ? "джерело" : "джерела";
}

const Card = ({ data, totalFiles, setTotalFiles }) => {
  const {
    id,
    lastName,
    firstName,
    patName,
    inn,
    passport,
    birthdate,
    address,
    subscribe,
    compared,
    sex,
    country,
    birthPlace,
    comment,
  } = data;
  const [sources, setSources] = useState({
    passport: false,
    inn: false,
    address: false,
  });

  const addToCompare = async (id) => {
    try {
      await fetch(`/api/user/comparePerson/${id}`, {
        method: "PUT",
        headers: authHeader(),
      }).then((res) => {
        if (res.status === 200) {
          dispatch(
            setAlertMessageThunk(
              `Користувач з ${id} доданий до об'єднання`,
              "success"
            )
          );
        }
      });
    } catch (error) {
      console.log(error);
    }
  };
  const [sub, setSub] = useState(subscribe);
  const [comp, setComp] = useState(compared);
  useEffect(() => {
    setSub(subscribe);
  }, [subscribe]);
  const userRole = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();
  const subscribeAction = (id) => {
    try {
      const res = userService.subscribe(id);
      if (res) {
        setSub(!sub);
        totalFiles && setTotalFiles(totalFiles + 1);
        dispatch(
          setAlertMessageThunk(
            `Користувач ${firstName ? firstName : ""} ${
              lastName ? lastName : ""
            } доданий до спостереження`,
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
      const res = userService.unsubscribe(id);
      if (res) {
        totalFiles && setTotalFiles(totalFiles - 1);
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
    } catch (e) {
      dispatch(setAlertMessageThunk("Виникла проблема"));
    }
  };
  const visible = useSelector((state) => state.auth.monitoring);

  return (
    <div className="row">
      <div className="col-md-4">
        <div className="card m-3 search">
          <div className="card_header d-flex align-items-center justify-content-between overflow-hidden">
            <IoIcons.IoMdPerson
              style={{
                width: 40,
                height: 40,
                fontWeight: "bold",
                marginLeft: "10px",
              }}
            />
            <h6 className="text-center">
              {lastName} {firstName} {patName}{" "}
              {sex && <span className={"ml-10"}>({sex})</span>}
            </h6>

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
          <div className="card-body overflow-hidden">
            {birthdate && (
              <div className="d-flex ">
                <b className="mr-10">Дата народження:</b>
                <p>{new DateObject(birthdate).format("DD.MM.YYYY")}</p>
              </div>
            )}

            {passport && (
              <div className="source-container">
                <b className="mr-10">Паспорт:</b>
                <p>{singlePassport(passport)}</p>

                <span
                  onClick={() =>
                    setSources({
                      passport: !sources.passport,
                    })
                  }
                  onMouseLeave={() =>
                    setTimeout(() => setSources({ passport: false }), 500)
                  }
                  className="ml-10 pointer"
                >
                  {passport
                    ? `(${passport?.importSources.length} ${sourceName(
                        passport?.importSources
                      )})`
                    : ""}
                </span>
                {((sources.passport && userRole === "ADVANCED") ||
                  (sources.passport && userRole === "ADMIN")) &&
                  passport.importSources.map((s) => {
                    return (
                      <ul className={"source"} key={s.id}>
                        <li>{s.name}</li>
                      </ul>
                    );
                  })}
              </div>
            )}

            {inn && (
              <div className="source-container">
                <b className="mr-10">ІПН:</b>
                <p>{singleInn(inn)}</p>

                <span
                  onClick={() =>
                    setSources({
                      inn: !sources.inn,
                    })
                  }
                  onMouseLeave={() =>
                    setTimeout(() => setSources({ inn: false }), 500)
                  }
                  className="ml-10 pointer"
                >
                  {inn
                    ? `(${inn.importSources.length} ${sourceName(
                        inn.importSources
                      )})`
                    : null}
                </span>
                {((sources.inn && userRole === "ADVANCED") ||
                  (sources.inn && userRole === "ADMIN")) &&
                  inn.importSources.map((s) => {
                    return (
                      <ul className={"source"} key={s.id}>
                        <li>{s.name}</li>
                      </ul>
                    );
                  })}
              </div>
            )}
            {address && (
              <div className="source-container">
                <div className={"pb-16"}>
                  <b className="mr-10">Адреса:</b>
                  <span>{address ? address.address : ""}</span>
                  {country && <span> &nbsp;{country}</span>}
                </div>
                <span
                  onClick={() =>
                    setSources({
                      address: !sources.address,
                    })
                  }
                  onMouseLeave={() =>
                    setTimeout(() => setSources({ address: false }), 500)
                  }
                  className="ml-10"
                >
                  {address
                    ? `(${address.importSources.length} ${sourceName(
                        address.importSources
                      )})`
                    : ""}
                </span>
                {((sources.address && userRole === "ADVANCED") ||
                  (sources.address && userRole === "ADMIN")) &&
                  address.importSources.map((s) => {
                    return (
                      <ul className={"source"} key={s.id}>
                        <li>{s.name}</li>
                      </ul>
                    );
                  })}
              </div>
            )}
            {birthPlace && (
              <p>
                <b className="mr-10">Місце народження:</b>
                <span>{birthPlace ? birthPlace : ""}</span>
              </p>
            )}
            {comment && (
              <p>
                <b className="mr-10">Коментар:</b>
                <span>{comment ? comment : ""}</span>
              </p>
            )}
          </div>

          <div className="card-footer d-flex justify-content-between align-items-center pointer">
            {userRole === "ADMIN" ? (
              <button
                onClick={() => {
                  addToCompare(id);
                  setComp(!comp);
                }}
                className={"border -0"}
                disabled={comp}
              >
                <IoIcons.IoIosGitCompare
                  title={"Об'єднати"}
                  style={{
                    width: 20,
                    height: 20,
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                />
              </button>
            ) : (
              <span />
            )}

            <Link className="text-dark" to={`/YPerson/${id}`}>
              Детальніше...
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Card;
