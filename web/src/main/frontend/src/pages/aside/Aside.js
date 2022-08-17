import React, { useState } from "react";

import { NavLink, useHistory } from "react-router-dom";
import SidebarData from "./SidebarData";
import { useDispatch, useSelector } from "react-redux";
import { IconContext } from "react-icons";
import * as IoIcons from "react-icons/io";
import { logoutUserThunk } from "../../store/reducers/AuthReducer";
import { version as app_version } from "../../../package.json";

const Aside = ({ build }) => {
  const userName = localStorage.getItem("userName");
  const [buildDate, setBuildDate] = useState(false);
  const userRole = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();
  const history = useHistory();
  const visible = useSelector((state) => state.auth.monitoring);
  return (
    <>
      <IconContext.Provider value={{ color: "#60aa18" }}>
        <div className="navbar">
          <div className="d-flex col-sm-12 justify-content-between align-items-center">
            <div className={"d-flex align-items-center"}>
              <h3 className="nav-title">
                Антифрод
                <span
                  onClick={() => setBuildDate(!buildDate)}
                  className={"ml-10 pointer"}
                >
                  {app_version}
                </span>
              </h3>
              {buildDate && (
                <small className={"ml-10 nav-title"}>{build}</small>
              )}
            </div>
            <span className="text-white p-2 user">
              {userName?.replace(/"/g, "")}
              <IoIcons.IoMdExit
                style={{ color: "white" }}
                onClick={() => {
                  dispatch(logoutUserThunk());
                  history.push("/");
                }}
                className="logout-btn ml-10"
              />
            </span>
          </div>
        </div>
        <nav className="nav-menu top">
          <ul className="nav-menu-items">
            <li className={SidebarData.Search.cName}>
              <NavLink to={SidebarData.Search.path}>
                {SidebarData.Search.icon}
                <span className="icons">{SidebarData.Search.title}</span>
              </NavLink>
            </li>

            {userRole === "ADVANCED" || userRole === "ADMIN" ? (
              <>
                {visible && (
                  <li className={SidebarData.Monitoring.cName}>
                    <NavLink to={SidebarData.Monitoring.path}>
                      {SidebarData.Monitoring.icon}
                      <span className="icons">
                        {SidebarData.Monitoring.title}
                      </span>
                    </NavLink>
                  </li>
                )}

                <li className={SidebarData.AddFile.cName}>
                  <NavLink to={SidebarData.AddFile.path}>
                    {SidebarData.AddFile.icon}
                    <span className="icons">{SidebarData.AddFile.title}</span>
                  </NavLink>
                </li>
                <li className={SidebarData.Files.cName}>
                  <NavLink to={SidebarData.Files.path}>
                    {SidebarData.Files.icon}
                    <span className="icons">{SidebarData.Files.title}</span>
                  </NavLink>
                </li>
                <li className={SidebarData.Progress.cName}>
                  <NavLink to={SidebarData.Progress.path}>
                    {SidebarData.Progress.icon}
                    <span className="icons">{SidebarData.Progress.title}</span>
                  </NavLink>
                </li>
                <li className={SidebarData.Sheduler.cName}>
                  <NavLink to={SidebarData.Sheduler.path}>
                    {SidebarData.Sheduler.icon}
                    <span className="icons">{SidebarData.Sheduler.title}</span>
                  </NavLink>
                </li>
                {userRole === "ADMIN" && (
                  <li className={SidebarData.Relations.cName}>
                    <NavLink to={SidebarData.Relations.path}>
                      {SidebarData.Relations.icon}
                      <span className="icons">
                        {SidebarData.Relations.title}
                      </span>
                    </NavLink>
                  </li>
                )}
              </>
            ) : (
              ""
            )}
          </ul>
        </nav>
      </IconContext.Provider>
    </>
  );
};

export default Aside;
