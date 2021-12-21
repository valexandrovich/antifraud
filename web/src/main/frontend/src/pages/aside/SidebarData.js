import React from "react";
import * as FaIcons from "react-icons/fa";

import * as IoIcons from "react-icons/io";

export const SidebarData = [
  {
    title: "Пошук",
    path: "/",
    icon: <IoIcons.IoIosSearch />,
    cName: "nav-text",
  },
  {
    title: "Перевірка БД",
    path: "/db_check",
    icon: <IoIcons.IoIosPaper />,
    cName: "nav-text",
  },
  {
    title: "Файли",
    path: "/uploaded_files",
    icon: <FaIcons.FaCartPlus />,
    cName: "nav-text",
  },
  //   {
  //     title: "Team",
  //     path: "/team",
  //     icon: <IoIcons.IoMdPeople />,
  //     cName: "nav-text",
  //   },
  {
    title: "Завантажити",
    path: "/add-file",
    icon: <FaIcons.FaEnvelopeOpenText />,
    cName: "nav-text",
  },
  // {
  //   title: "Support",
  //   path: "/support",
  //   icon: <IoIcons.IoMdHelpCircle />,
  //   cName: "nav-text",
  // },
];
