import React from "react";

import { Link } from "react-router-dom";
import { SidebarData } from "./SidebarData";

import { IconContext } from "react-icons";
import * as IoIcons from "react-icons/io";
function Aside() {
  return (
    <>
      <IconContext.Provider value={{ color: "#fff" }}>
        <div className="navbar">
          <div className="d-flex col-md-12 justify-content-end align-items-center">
            <h3 className="nav-title">Антифрод</h3>
            <span className="text-white">Венгер А.Б.</span>
            <IoIcons.IoMdExit className="logout-btn" />
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
