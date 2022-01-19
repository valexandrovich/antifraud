import React from "react";

import { Link } from "react-router-dom";
import { SidebarData } from "./SidebarData";
import { useDispatch } from "react-redux";
import { IconContext } from "react-icons";
import * as IoIcons from "react-icons/io";
import { logoutUserThunk } from "../../store/reducers/AuthReducer";
function Aside() {
  const userName = localStorage.getItem("userName");
  const dispatch = useDispatch();
  return (
    <>
      <IconContext.Provider value={{ color: "#fff" }}>
        <div className="navbar">
          <div className="d-flex col-md-12 justify-content-end align-items-center">
            <h3 className="nav-title">Антифрод</h3>
            <span className="text-white p-2">{userName.replace(/"/g, "")}</span>

            <IoIcons.IoMdExit
              onClick={() => dispatch(logoutUserThunk())}
              className="logout-btn"
            />
          </div>
        </div>
        <nav className="nav-menu active">
          <ul className="nav-menu-items">
            {SidebarData.map((item, index) => {
              return (
                <li key={index} className={item.cName}>
                  <Link to={item.path}>
                    {item.icon}
                    <span className="icons">{item.title}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </IconContext.Provider>
    </>
  );
}

export default Aside;
