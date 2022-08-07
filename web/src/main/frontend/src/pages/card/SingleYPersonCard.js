import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageTitle from "../../common/PageTitle";
import * as IoIcons from "react-icons/io";
import Passports from "../../components/YPersonCard/Passports";
import Inn from "../../components/YPersonCard/Inn";
import Addresses from "../../components/YPersonCard/Addresses";
import Email from "../../components/YPersonCard/Email";
import Tags from "../../components/YPersonCard/Tags";
import Phone from "../../components/YPersonCard/Phone";
import authHeader from "../../api/AuthHeader";
import jsPDF from "jspdf";
import html2canvas from "html2canvas";
import AltPerson from "../../components/YPersonCard/AltPerson";
import ToggleCard from "../../common/ToggleCard";
import { DateObject } from "react-multi-date-picker";
import {
  formatInn,
  formatPassport,
  sourceName,
} from "../../components/YPersonCard/Card";
import passportService from "../../api/YPassportApi";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";
import { useDispatch } from "react-redux";
import Relations from "../../components/YPersonCard/Relations";

const SingleYPersonCard = () => {
  let { id } = useParams();
  const dispatch = useDispatch();
  const [personDetails, setPersonDetails] = useState(null);
  const [components, setComponents] = useState({
    passports: true,
    inn: true,
    address: true,
    email: true,
    tag: true,
    phone: true,
    alt: true,
    relations: true,
  });
  const deletePassport = (id) => {
    passportService.deletePassport(id).then((res) => {
      dispatch(setAlertMessageThunk("Пасспорт успішно видалено", "success"));
      setPersonDetails((prevState) => ({
        ...prevState,
        passports: prevState.passports.filter((passport) => passport.id !== id),
      }));
    });
  };

  const handleChange = (e, type) => {
    debugger;
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
    window.scrollTo(0, 0);
    const divToPrint = document.querySelector("#print");
    html2canvas(divToPrint).then((canvas) => {
      const imgData = canvas.toDataURL("image/png");
      const imgWidth = 190;
      const pageHeight = 290;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      let heightLeft = imgHeight;
      const doc = new jsPDF("pt", "mm");
      let position = 20;
      doc.addImage(imgData, "PNG", 10, 0, imgWidth, imgHeight);
      heightLeft -= pageHeight;
      while (heightLeft >= 0) {
        position = heightLeft - imgHeight;
        doc.addPage();
        doc.addImage(imgData, "PNG", 10, position, imgWidth, imgHeight + 45);
        debugger;
        heightLeft -= pageHeight;
      }
      doc.save(`звіт ${personDetails.lastName} ${personDetails.firstName}.pdf`);
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
                  {personDetails.birthdate
                    ? new DateObject(personDetails.birthdate).format(
                        "DD.MM.YYYY"
                      )
                    : null}
                </p>
              </div>
              <div className="d-flex">
                <b className="mr-10">ІПН:</b>
                <p>{formatInn(personDetails.inns)}</p>
                <span className="ml-10">
                  {personDetails.inns && personDetails.inns.length > 0
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
                  {personDetails.passports && personDetails.passports.length > 0
                    ? `(${
                        personDetails.passports[0].importSources.length
                      } ${sourceName(
                        personDetails.passports[0].importSources
                      )})`
                    : " "}
                </span>
              </div>

              <div className="d-flex">
                <b className="mr-10">Адреса:</b>
                <p>
                  {personDetails.addresses && personDetails.addresses.length > 0
                    ? personDetails.addresses[0].address
                    : " "}
                </p>
                <span className="ml-10">
                  {personDetails.addresses && personDetails.addresses.length > 0
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
              children={
                personDetails.passports &&
                personDetails.passports.map((passport) => (
                  <Passports
                    onClick={() => deletePassport(passport.id)}
                    onChange={(e) => handleChange(e, "passports")}
                    key={passport.id}
                    data={passport}
                  />
                ))
              }
            />

            <ToggleCard
              setComponents={setComponents}
              components={components.inn}
              name={"ІПН"}
              type={"inn"}
              element={personDetails.inns}
              children={
                personDetails.inns &&
                personDetails.inns.map((inn) => (
                  <Inn
                    onChange={(e) => handleChange(e, "inns")}
                    key={inn.id}
                    data={inn}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.address}
              name={"Адреси"}
              type={"address"}
              element={personDetails.addresses}
              children={
                personDetails.addresses &&
                personDetails.addresses.map((address) => (
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
              components={components.email}
              name={"Emails"}
              type={"email"}
              element={personDetails.emails}
              children={
                personDetails.emails &&
                personDetails.emails.map((email) => (
                  <Email
                    onChange={(e) => handleChange(e, "emails")}
                    key={email.id}
                    data={email}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.tag}
              name={"Теги"}
              type={"tag"}
              element={personDetails.tags}
              children={
                personDetails.tags &&
                personDetails.tags.map((tag) => (
                  <Tags
                    onChange={(e) => handleChange(e, "tags")}
                    key={tag.id}
                    data={tag}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.phone}
              name={"Телефони"}
              type={"phone"}
              element={personDetails.phones}
              children={
                personDetails.phones &&
                personDetails.phones.map((phone) => (
                  <Phone
                    onChange={(e) => handleChange(e, "phones")}
                    key={phone.id}
                    data={phone}
                  />
                ))
              }
            />

            <ToggleCard
              setComponents={setComponents}
              components={components.alt}
              name={"Альтернативні імена"}
              type={"alt"}
              element={personDetails.altPeople}
              children={
                personDetails.altPeople &&
                personDetails.altPeople.map((alt) => (
                  <AltPerson
                    onChange={(e) => handleChange(e, "altPeople")}
                    key={alt.id}
                    data={alt}
                  />
                ))
              }
            />
            <ToggleCard
              setComponents={setComponents}
              components={components.relations}
              name={"Зв'язки"}
              type={"relations"}
              element={personDetails.relationGroups}
            >
              {personDetails.relationGroups &&
                personDetails.relationGroups.length > 0 &&
                personDetails.relationGroups.map((rel) => (
                  <Relations
                    personId={id}
                    onChange={(e) => handleChange(e, "personRelations")}
                    key={rel.id}
                    data={rel}
                  />
                ))}
            </ToggleCard>
          </div>
        </div>
      )}
    </div>
  );
};

export default SingleYPersonCard;
