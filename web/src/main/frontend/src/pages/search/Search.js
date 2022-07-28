import React from "react";
import FizFormSearch from "../../components/Forms/FizFormSearch";
import UrFormSearch from "../../components/Forms/UrFormSearch";
import Tabs from "../../components/Forms/Tabs";
import { useSelector } from "react-redux";

const Search = () => {
  const tab = useSelector((state) => state.auth.activeTab);
  return (
    <div className="wrapped">
      <div id="page-wrapper" className="gray-bg">
        <div className="wrapper wrapper-content animated fadeInRight">
          <div className="row form_bg">
            <div className="col-lg-12 mt-2">
              <Tabs />
              {tab === "fiz" && <FizFormSearch />}
              {tab === "ur" && <UrFormSearch />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Search;
