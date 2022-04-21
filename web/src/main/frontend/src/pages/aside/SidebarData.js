import React from "react";

import * as FaIcons from "react-icons/fa";

import * as IoIcons from "react-icons/io";
const Search = {
  title: "Пошук",
  path: "/",
  icon: <IoIcons.IoIosSearch />,
  cName: "nav-text",
};

const AddFile = {
  title: "Завантажити",
  path: "/add-file",
  icon: <FaIcons.FaEnvelopeOpenText />,
  cName: "nav-text",
};

const Files = {
  title: "Файли",
  path: "/uploaded_files",
  icon: <FaIcons.FaCartPlus />,
  cName: "nav-text",
};

const DB = {
  title: "Перевірка БД",
  path: "/db_check",
  icon: <IoIcons.IoIosPaper />,
  cName: "nav-text",
};
const Progress = {
  title: "Прогрес",
  path: "/progress",
  icon: <IoIcons.IoIosPulse />,
  cName: "nav-text",
};

const SidebarData = {
  Search,
  Files,
  AddFile,
  DB,
  Progress,
};

export default SidebarData;
