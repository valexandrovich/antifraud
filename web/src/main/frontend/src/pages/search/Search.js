import React, { useState } from "react";
import FizFormSearch from "../../components/FizFormSearch";
import UrFormSearch from "../../components/UrFormSearch";

const Search = () => {
  const [activeTab, setActiveTab] = useState("fiz");
  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <ul className="nav nav-tabs">
                <li className="nav-item color-success col-sm-6">
                  <button
                    onClick={() => setActiveTab("fiz")}
                    className={
                      activeTab === "fiz"
                        ? "nav-link active bg-success fullWidth text-white"
                        : "nav-link fullWidth .bg-light.bg-gradient bg-opacity-30 p-2 text-success"
                    }
                  >
                    Фізичні особи
                  </button>
                </li>
                <li className="nav-item col-sm-6">
                  <button
                    onClick={() => setActiveTab("ur")}
                    className={
                      activeTab === "ur"
                        ? "nav-link active bg-success  fullWidth text-white"
                        : "nav-link fullWidth .bg-light.bg-gradient text-success"
                    }
                  >
                    Юридичні особи
                  </button>
                </li>
              </ul>
              {activeTab === "fiz" && <FizFormSearch />}
              {activeTab === "ur" && <UrFormSearch />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Search;
