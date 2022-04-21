import React from "react";
import { Link } from "react-router-dom";

const PageTitle = (props) => {
  const getTree = (page, isTitle = false) => {
    const tree = {
      main: {
        name: "Головна",
        link: "/",
      },
      upload: {
        name: "Завантажити",
        link: "/upload",
      },
      db_check: {
        name: "Перевірка БД",
        link: "/db_check",
      },
      uploaded_files: {
        name: "Файлы",
        link: "/uploaded_files",
      },
      progress: {
        name: "Прогрес завантаження",
        link: "/progress",
      },
    };

    if (isTitle) {
      return tree[page].name;
    }
    let breadcrumps =
      tree[page] && tree[page].parent
        ? tree[page].parent.map((e, idx) => {
            return (
              <li key={idx} className="breadcrumb-item">
                <Link to={tree[e].link}>{tree[e].name}</Link>
              </li>
            );
          })
        : [];

    return [
      ...breadcrumps,
      <li className="breadcrumb-item active">{tree[page].name}</li>,
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
                <Link to={"/"}>Пошук</Link>
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
