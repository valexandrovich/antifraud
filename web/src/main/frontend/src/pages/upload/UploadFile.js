import React from "react";

import PageTitle from "../../components/PageTitle";
import Dropzone from "../../components/Drag";

const UploadFile = () => {
  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <PageTitle title={"upload"} />
          <div className="nav_bg_color" id="wrapper">
            <div id="page-wrapper" className="gray-bg">
              <div className="row form_bg">
                <div className="col-lg-12">
                  <Dropzone />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadFile;
