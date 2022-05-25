import React from "react";

import * as FaIcons from "react-icons/fa";

import * as IoIcons from "react-icons/io";
const Search = {
  title: "Пошук",
  path: "/search",
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

const Progress = {
  title: "Прогрес",
  path: "/progress",
  icon: <IoIcons.IoIosPulse />,
  cName: "nav-text",
};
const Sheduler = {
  title: "Розклад",
  path: "/sheduler",
  icon: <IoIcons.IoIosAlarm />,
  cName: "nav-text",
};
const Monitoring = {
  title: "Моніторинг",
  path: "/subscription",
  icon: <IoIcons.IoMdStar />,
  cName: "nav-text",
};
const SidebarData = {
  Search,
  Files,
  AddFile,
  Progress,
  Sheduler,
  Monitoring,
};

export default SidebarData;
