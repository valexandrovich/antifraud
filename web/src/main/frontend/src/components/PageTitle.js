import React from "react";
import { Link } from "react-router-dom";
import { v4 as uuid } from "uuid";

const PageTitle = (props) => {
  const getTree = (page, isTitle = false) => {
    const tree = {
      main: {
        name: "Пошук",
        link: "/search",
        id: 1,
      },
      upload: {
        name: "Завантажити",
        link: "/upload",
        id: 2,
      },

      uploaded_files: {
        name: "Файлы",
        link: "/uploaded_files",
        id: 3,
      },
      progress: {
        name: "Прогрес завантаження",
        link: "/progress",
        id: 4,
      },
      sheduler: {
        name: "Розклад",
        link: "/sheduler",
        id: 5,
      },
      details: {
        name: "Детальна інформація",
        link: "/details",
        id: 6,
      },
      monitoring: {
        name: "Моніторинг",
        link: "/subscription",
        id: 7,
      },
    };

    if (isTitle) {
      return tree[page].name;
    }
    let breadcrumbs =
      tree[page] && tree[page].parent
        ? tree[page].parent.map((e) => {
            return (
              <li key={e.id} className="breadcrumb-item">
                <Link to={tree[e].link}>{tree[e].name}</Link>
              </li>
            );
          })
        : [];

    return [
      ...breadcrumbs,
      <li key={uuid()} className="breadcrumb-item active">
        {tree[page].name}
      </li>,
    ];
  };

  return (
    <div className="content-header">
      <div className="container-fluid">
        <div className="row mb-2">
          <div className="col-sm-12">
            <h1 className="m-0">{getTree(props.title, true)}</h1>
          </div>
          <div className="col-sm-6">
            <ol className="breadcrumb float-sm-right">
              <li className="breadcrumb-item">
                <Link to={"/search"}>Пошук</Link>
              </li>
              {getTree(props.title)}
            </ol>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PageTitle;
