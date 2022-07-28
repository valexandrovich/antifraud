import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { toggleTab } from "../../store/reducers/actions/Actions";

const Tabs = () => {
  const tab = useSelector((state) => state.auth.activeTab);
  const dispatch = useDispatch();
  return (
    <ul className="nav nav-tabs">
      <li className="nav-item col-sm-6">
        <button
          onClick={() => dispatch(toggleTab("fiz"))}
          className={
            tab === "fiz"
              ? "btn active custom-btn fullWidth text-white"
              : "nav-link fullWidth .bg-light.bg-gradient bg-opacity-30 p-2 text-success"
          }
        >
          Фізичні особи
        </button>
      </li>
      <li className="nav-item col-sm-6">
        <button
          onClick={() => dispatch(toggleTab("ur"))}
          className={
            tab === "ur"
              ? "btn active custom-btn  fullWidth text-white"
              : "nav-link fullWidth .bg-light.bg-gradient text-success"
          }
        >
          Юридичні особи
        </button>
      </li>
    </ul>
  );
};

export default Tabs;
