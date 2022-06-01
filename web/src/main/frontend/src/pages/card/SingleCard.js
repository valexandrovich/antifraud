import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
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
import {DateObject} from "react-multi-date-picker";

function pad(num, size) {
    const numLength = num.toString().length;
    let zeroCount = size - numLength;
    while (zeroCount > 0) {
        num = "0" + num;
        zeroCount--;
    }
    return num;
}

function formatPassport(passports) {
    if (passports.length > 0) {
        if (passports[0].type === "UA_IDCARD") {
            return pad(passports[0].number, 9);
        } else {
            return `${passports[0].series}${pad(passports[0].number, 6)}`;
        }
    }
    return "";
}

const SingleCard = () => {
    let {id} = useParams();
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

    useEffect(() => {
        const getPersonDetails = async () => {
            try {
                const response = await fetch(`/api/uniPF/find/${id}`, {
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
            <PageTitle title={"details"}/>
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

                                <p>{new DateObject(personDetails.birthdate).format("DD.MM.YYYY")}</p>
                            </div>
                            <div className="d-flex">
                                <b className="mr-10">ІПН:</b>
                                <p>
                                    {personDetails.inns.length > 0
                                        ? personDetails.inns[0].inn
                                        : ""}
                                </p>
                            </div>
                            <div className="d-flex">
                                <b className="mr-10">Паспорт:</b>
                                <p>
                                    {formatPassport(personDetails.passports)}
                                </p>
                            </div>

                            <div className="d-flex">
                                <b className="mr-10">Адреса:</b>
                                <p>
                                    {personDetails.addresses.length > 0
                                        ? personDetails.addresses[0].address
                                        : ""}
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="card-body">
                        <div
                            className={
                                components.passports ? "form-control mb-3" : "form-select mb-3"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    passports: !prevState.passports,
                                }));
                            }}
                        >
                            Паспорти
                            {components.passports &&
                            personDetails.passports.map((pasport) => (
                                <Passports key={pasport.id} data={pasport}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.inn ? "form-control mb-3" : "form-select mb-3"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    inn: !prevState.inn,
                                }));
                            }}
                        >
                            ИПН
                            {components.inn &&
                            personDetails.inns.map((inn) => (
                                <Inn key={inn.id} data={inn}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.address
                                    ? "form-control mb-3 pointer"
                                    : "form-select mb-3 pointer"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    address: !prevState.address,
                                }));
                            }}
                        >
                            Адреси
                            {components.address &&
                            personDetails.addresses.map((address) => (
                                <Addresses key={address.id} data={address}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.email
                                    ? "form-control mb-3 pointer"
                                    : "form-select mb-3 pointer"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    email: !prevState.email,
                                }));
                            }}
                        >
                            Email
                            {components.email &&
                            personDetails.emails.map((email) => (
                                <Email key={email.id} data={email}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.tag
                                    ? "form-control mb-3 pointer"
                                    : "form-select mb-3 pointer"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    tag: !prevState.tag,
                                }));
                            }}
                        >
                            Теги
                            {components.tag &&
                            personDetails.tags.map((tag) => (
                                <Tags key={tag.id} data={tag}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.phone
                                    ? "form-control mb-3 pointer"
                                    : "form-select mb-3 pointer"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    phone: !prevState.phone,
                                }));
                            }}
                        >
                            Телефони
                            {components.phone &&
                            personDetails &&
                            personDetails.phones.map((phone) => (
                                <Phone key={phone.id} data={phone}/>
                            ))}
                        </div>
                        <div
                            className={
                                components.alt
                                    ? "form-control mb-3 pointer"
                                    : "form-select mb-3 pointer"
                            }
                            onClick={() => {
                                setComponents((prevState) => ({
                                    ...prevState,
                                    alt: !prevState.alt,
                                }));
                            }}
                        >
                            Альтернативні імена
                            {components.alt &&
                            personDetails &&
                            personDetails.altPeople.map((alt) => (
                                <AltPerson key={alt.id} data={alt}/>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SingleCard;
