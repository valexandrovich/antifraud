import React from "react";

import { NavLink } from "react-router-dom";
import SidebarData from "./SidebarData";
import { useDispatch, useSelector } from "react-redux";
import { IconContext } from "react-icons";
import * as IoIcons from "react-icons/io";
import { logoutUserThunk } from "../../store/reducers/AuthReducer";
const Aside = () => {
  const userName = localStorage.getItem("userName");
  const role = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();

  return (
    <>
      <IconContext.Provider value={{ color: "#60aa18" }}>
        <div className="navbar">
          <div className="d-flex col-sm-12 justify-content-between align-items-center">
            <h3 className="nav-title">Антифрод</h3>
            <span className="text-white p-2 user">
              {userName?.replace(/"/g, "")}
              <IoIcons.IoMdExit
                style={{ color: "white" }}
                onClick={() => dispatch(logoutUserThunk())}
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
            <li className={SidebarData.Monitoring.cName}>
              <NavLink to={SidebarData.Monitoring.path}>
                {SidebarData.Monitoring.icon}
                <span className="icons">
                  {SidebarData.Monitoring.title}
                </span>
              </NavLink>
            </li>
            {role === "ADVANCED" ? (
              <>
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
