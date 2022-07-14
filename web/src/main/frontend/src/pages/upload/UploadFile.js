import React, { useState } from "react";

import PageTitle from "../../components/PageTitle";
import Dropzone from "../../components/Dropzone";

const UploadFile = () => {
  const [activeTab, setActiveTab] = useState("fiz");
  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <PageTitle title={"upload"} />
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <ul className="nav nav-tabs">
                <li className="nav-item col-sm-6">
                  <button
                    onClick={() => setActiveTab("fiz")}
                    className={
                      activeTab === "fiz"
                        ? "btn active custom-btn fullWidth text-white"
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
                        ? "btn active custom-btn  fullWidth text-white"
                        : "nav-link fullWidth .bg-light.bg-gradient text-success"
                    }
                  >
                    Юридичні особи
                  </button>
                </li>
              </ul>
              {activeTab === "fiz" && <Dropzone />}
              {activeTab === "ur" && <h1>ЮРИДИЧНІ ОСОБИ</h1>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadFile;
