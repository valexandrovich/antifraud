import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageTitle from "../../components/PageTitle";
import * as IoIcons from "react-icons/io";
import Passports from "../../components/Card/Passports";
import Inn from "../../components/Card/Inn";
import Addresses from "../../components/Card/Addresses";
import Email from "../../components/Card/Email";
import Tags from "../../components/Card/Tags";
import Phone from "../../components/Card/Phone";
import authHeader from "../../api/AuthHeader";
import jsPDF from "jspdf";
import html2canvas from "html2canvas";
import AltPerson from "../../components/Card/AltPerson";
import ToggleCard from "../../components/Card/ToggleCard";
import { DateObject } from "react-multi-date-picker";
import {
  formatInn,
  formatPassport,
  sourceName,
} from "../../components/Card/Card";

const SingleCard = () => {
  let { id } = useParams();

  const [personDetails, setPersonDetails] = useState(null);
  const [components, setComponents] = useState({
    passports: false,
    inn: false,
    address: false,
    email: false,
    tag: false,
    phone: false,
    alt: false,
  });
  const handleChange = (e, type) => {
    const { name, value, checked, id } = e.target;
    setPersonDetails((prevState) => ({
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
  useEffect(() => {
    const getPersonDetails = async () => {
      try {
        const response = await fetch(`/api/yperson/find/${id}`, {
          method: "GET",
          headers: authHeader(),
        });
        const res = await response.json();
        setPersonDetails(res);
      } catch (error) {
        console.log(error);
      }
    };
    getPersonDetails();
  }, [id]);
  const exportPdf = () => {
    html2canvas(document.querySelector("#print")).then((canvas) => {
      const imgData = canvas.toDataURL("image/png");
      const pdf = new jsPDF();
      pdf.addImage(imgData, "PNG", 0, 0);
      pdf.save(`звіт ${personDetails.lastName} ${personDetails.firstName}.pdf`);
    });
  };

  return (
    <div className="wrapped">
      <PageTitle title={"details"} />
      <div className="d-flex justify-content-end">
        <button className="btn custom-btn p-2" onClick={exportPdf}>
          Звіт PDF
        </button>
      </div>
      {personDetails && (
        <div className="card" id="print">
          <div className="card-header d-flex">
            <IoIcons.IoMdPerson
              style={{
                width: 180,
                height: 180,
                fontWeight: "bold",
              }}
            />

            <div className="d-flex flex-column">
              <div className="d-flex">
                <h5>
                  <b>
                    {personDetails.lastName} {""}
                    {personDetails.firstName} {""}
                    {personDetails.patName}
                  </b>
                </h5>
              </div>
              <div className="d-flex">
                <b className="mr-10">Дата народження:</b>

                <p>
                  {new DateObject(personDetails.birthdate).format("DD.MM.YYYY")}
                </p>
              </div>
              <div className="d-flex">
                <b className="mr-10">ІПН:</b>
                <p>{formatInn(personDetails.inns)}</p>
                <span className="ml-10">
                  {personDetails.inns.length > 0
                    ? `(${
                        personDetails.inns[0].importSources.length
                      } ${sourceName(personDetails.inns[0].importSources)})`
                    : ""}
                </span>
              </div>
              <div className="d-flex">
                <b className="mr-10">Паспорт:</b>
                <p>{formatPassport(personDetails.passports)}</p>
                <span className="ml-10">
                  {personDetails.passports.length > 0
                    ? `(${
                        personDetails.passports[0].importSources.length
                      } ${sourceName(
                        personDetails.passports[0].importSources
                      )})`
                    : ""}
                </span>
              </div>

              <div className="d-flex">
                <b className="mr-10">Адреса:</b>
                <p>
                  {personDetails.addresses.length > 0
                    ? personDetails.addresses[0].address
                    : ""}
                </p>
                <span className="ml-10">
                  {personDetails.addresses.length > 0
                    ? `(${
                        personDetails.addresses[0].importSources.length
                      } ${sourceName(
                        personDetails.addresses[0].importSources
                      )})`
                    : ""}
                </span>
              </div>
            </div>
          </div>

          <div className="card-body">
            <ToggleCard
              setComponents={setComponents}
              components={components.passports}
              name={"Паспорти"}
              type={"passports"}
              element={personDetails.passports}
              children={personDetails.passports.map((passport) => (
                <Passports
                  onChange={(e) => handleChange(e, "passports")}
                  key={passport.id}
                  data={passport}
                />
              ))}
            />

            <ToggleCard
              setComponents={setComponents}
              components={components.inn}
              name={"ІПН"}
              type={"inn"}
              element={personDetails.inns}
              children={personDetails.inns.map((inn) => (
                <Inn
                  onChange={(e) => handleChange(e, "inns")}
                  key={inn.id}
                  data={inn}
                />
              ))}
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.address}
              name={"Адреси"}
              type={"address"}
              element={personDetails.addresses}
              children={personDetails.addresses.map((address) => (
                <Addresses
                  onChange={(e) => handleChange(e, "addresses")}
                  key={address.id}
                  data={address}
                />
              ))}
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.email}
              name={"Emails"}
              type={"email"}
              element={personDetails.emails}
              children={personDetails.emails.map((email) => (
                <Email
                  onChange={(e) => handleChange(e, "emails")}
                  key={email.id}
                  data={email}
                />
              ))}
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.tag}
              name={"Теги"}
              type={"tag"}
              element={personDetails.tags}
              children={personDetails.tags.map((tag) => (
                <Tags key={tag.id} data={tag} />
              ))}
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.phone}
              name={"Телефони"}
              type={"phone"}
              element={personDetails.phones}
              children={personDetails.phones.map((phone) => (
                <Phone
                  onChange={(e) => handleChange(e, "phones")}
                  key={phone.id}
                  data={phone}
                />
              ))}
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.alt}
              name={"Альтернативні імена"}
              type={"alt"}
              element={personDetails.altPeople}
              children={personDetails.altPeople.map((alt) => (
                <AltPerson
                  onChange={(e) => handleChange(e, "altPeople")}
                  key={alt.id}
                  data={alt}
                />
              ))}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default SingleCard;
