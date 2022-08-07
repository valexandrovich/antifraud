import React, { useEffect, useState } from "react";
import PageTitle from "../../common/PageTitle";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import authHeader from "../../api/AuthHeader";
import { useParams } from "react-router-dom";
import * as IoIcons from "react-icons/io";
import { sourceName } from "../../components/YPersonCard/Card";
import ToggleCard from "../../common/ToggleCard";
import Addresses from "../../components/YPersonCard/Addresses";
import Tags from "../../components/YPersonCard/Tags";
import AltCompanies from "../../components/YCompanyCard/AltCompanies";

const SingleYCompanyCard = () => {
  let { id } = useParams();
  const [companyDetails, setCompanyDetails] = useState(null);
  const [components, setComponents] = useState({
    address: true,
    pdv: true,
    edrpou: true,
    name: true,
    tags: true,
    alt: true,
  });

  useEffect(() => {
    const getCompanyDetails = async () => {
      try {
        const response = await fetch(`/api/ycompany/find/${id}`, {
          method: "GET",
          headers: authHeader(),
        });
        const res = await response.json();
        setCompanyDetails(res);
      } catch (error) {
        console.log(error);
      }
    };
    getCompanyDetails();
  }, [id]);

  const exportPdf = () => {
    html2canvas(document.querySelector("#print")).then((canvas) => {
      const imgData = canvas.toDataURL("image/png");
      const pdf = new jsPDF();
      pdf.addImage(imgData, "PNG", 10, 10);
      pdf.save(`звіт ${companyDetails.edrpou}.pdf`);
    });
  };
  const handleChange = (e, type) => {
    const { name, value, checked, id } = e.target;

    setCompanyDetails((prevState) => ({
      ...prevState,
      [type]: prevState[type].map((el) => {
        if (el.id === Number(id)) {
          if (name === "validity") {
            return { ...el, [name]: checked };
          }
          return { ...el, [name]: value };
        } else {
          return el;
        }
      }),
    }));
  };
  return (
    <div className="wrapped">
      <PageTitle title={"details"} />
      <div className="d-flex justify-content-end">
        <button className="btn custom-btn p-2" onClick={exportPdf}>
          Звіт PDF
        </button>
      </div>
      {companyDetails && (
        <div className="card" id="print">
          <div className="card-header d-flex">
            <IoIcons.IoMdBusiness
              style={{
                width: 180,
                height: 180,
                fontWeight: "bold",
              }}
            />

            <div className="d-flex flex-column">
              <div className="d-flex">
                <h5>
                  <b>{companyDetails.name}</b>
                </h5>
              </div>
              <div className="d-flex">
                <b className="mr-10">ЄДРПОУ:</b>
                <p>{companyDetails.edrpou ? companyDetails.edrpou : null}</p>
              </div>
              <div className="d-flex">
                <b className="mr-10">Код платника ПДВ:</b>
                <p>{companyDetails.pdv}</p>
                <span className="ml-10">
                  {companyDetails.pdv && companyDetails.pdv.length > 0
                    ? `(${
                        companyDetails.pdv[0].importSources.length
                      } ${sourceName(companyDetails.pdv[0].importSources)})`
                    : ""}
                </span>
              </div>
            </div>
          </div>
          <div className="card-body">
            <ToggleCard
              setComponents={setComponents}
              components={components.alt}
              name={"Альтернативні компанії"}
              type={"alt"}
              element={companyDetails.altCompanies}
              children={
                companyDetails.altCompanies &&
                companyDetails.altCompanies.map((alt) => (
                  <AltCompanies
                    onChange={(e) => handleChange(e, "altCompanies")}
                    key={alt.id}
                    data={alt}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.address}
              name={"Адреси"}
              type={"address"}
              element={companyDetails.addresses}
              children={
                companyDetails.addresses &&
                companyDetails.addresses.map((address) => (
                  <Addresses
                    onChange={(e) => handleChange(e, "addresses")}
                    key={address.id}
                    data={address}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.tag}
              name={"Теги"}
              type={"tag"}
              element={companyDetails.tags}
              children={
                companyDetails.tags &&
                companyDetails.tags.map((tag) => (
                  <Tags
                    onChange={(e) => handleChange(e, "tags")}
                    key={tag.id}
                    data={tag}
                  />
                ))
              }
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default SingleYCompanyCard;
