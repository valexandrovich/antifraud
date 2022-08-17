import React from "react";

import PageTitle from "../../common/PageTitle";
import Dropzone from "../../components/Dropzone/Dropzone";
import { useSelector } from "react-redux";
import Tabs from "../../components/Forms/Tabs";
import DropzoneJuridical from "../../components/Dropzone/DropzoneJuridical";

const UploadFile = () => {
  const tab = useSelector((state) => state.auth.activeTab);
  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <PageTitle title={"upload"} />
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <Tabs />
              {tab === "fiz" && <Dropzone />}
              {tab === "ur" && <DropzoneJuridical />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadFile;
