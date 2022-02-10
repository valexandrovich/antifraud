import React from "react";

import { Link } from "react-router-dom";
import SidebarData from "./SidebarData";
import { useDispatch, useSelector } from "react-redux";
import { IconContext } from "react-icons";
import * as IoIcons from "react-icons/io";
import { logoutUserThunk } from "../../store/reducers/AuthReducer";
const Aside =()=> {
  const userName = localStorage.getItem("userName") || "test";
  const role = useSelector((state) => state.auth.role);
  const dispatch = useDispatch();
  return (
      <>
        <IconContext.Provider value={{ color: "#fff" }}>
          <div className="navbar">
            <div className="d-flex col-md-12 justify-content-end align-items-center">
              <h3 className="nav-title">Антифрод</h3>
              <span className="text-white p-2">
              {userName?.replace(/"/g, "")}
            </span>

              <IoIcons.IoMdExit
                  onClick={() => dispatch(logoutUserThunk())}
                  className="logout-btn"
              />
            </div>
          </div>
          <nav className="nav-menu active">
            <ul className="nav-menu-items">
              <li className={SidebarData.Search.cName}>
                <Link to={SidebarData.Search.path}>
                  {SidebarData.Search.icon}
                  <span className="icons">{SidebarData.Search.title}</span>
                </Link>
              </li>
              {role === "ADVANCED" ? (
                  <li className={SidebarData.AddFile.cName}>
                    <Link to={SidebarData.AddFile.path}>
                      {SidebarData.AddFile.icon}
                      <span className="icons">{SidebarData.AddFile.title}</span>
                    </Link>
                  </li>
              ) : (
                  ""
              )}

              <li className={SidebarData.Files.cName}>
                <Link to={SidebarData.Files.path}>
                  {SidebarData.Files.icon}
                  <span className="icons">{SidebarData.Files.title}</span>
                </Link>
              </li>
              <li className={SidebarData.DB.cName}>
                <Link to={SidebarData.DB.path}>
                  {SidebarData.DB.icon}
                  <span className="icons">{SidebarData.DB.title}</span>
                </Link>
              </li>
            </ul>
          </nav>
        </IconContext.Provider>
      </>
  );
}

export default Aside;