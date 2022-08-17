import React from "react";
import PageTitle from "../../common/PageTitle";
import { useSelector } from "react-redux";
import Tabs from "../../components/Forms/Tabs";
import Yperson from "../../components/Monitoring/Yperson";
import YCompany from "../../components/Monitoring/YCompany";

const Monitoring = () => {
  const tab = useSelector((state) => state.auth.activeTab);

  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <PageTitle title={"monitoring"} />
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <Tabs />
              {tab === "fiz" && <Yperson />}
              {tab === "ur" && <YCompany />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Monitoring;
