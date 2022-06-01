import React, {useState, useEffect} from "react";
import {Link} from "react-router-dom";
import Card from "../../components/Card/Card";
import PageTitle from "../../components/PageTitle";
import Pagination from "../../components/Pagination";

const Monitoring = () => {
    const [subscribed, setSubscribed] = useState([]);
    const [pageSize, setPageSize] = useState(6);
    const [pageNo, setPageNo] = useState(0);
    const [totalFiles, setTotalFiles] = useState();
    const paginate = (pageNumber) => setPageNo(pageNumber - 1);
    useEffect(() => {
        const getSubscribed = async () => {
            try {
                const response = await fetch("/api/user/subscriptions", {
                    method: "POST",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        Authorization:
                            "Bearer " + localStorage.getItem("user").replace(/"/g, ""),
                    },
                    body: JSON.stringify({
                        direction: "ASC",
                        page: pageNo,
                        properties: [
                            "id"
                        ],
                        size: pageSize
                    })
                });
                const res = await response.json();
                setTotalFiles(res.totalElements);
                setSubscribed(res.content);
            } catch (error) {
                console.log(error);
            }
        };
        getSubscribed(pageNo, pageSize);
    }, [pageSize, pageNo,]);

    return (
        <div className="wrapped">

            <PageTitle title={"monitoring"}/>
            <div className="d-flex justify-content-end">
                <div className="form-group col-md-2 mb-2">
                    <label htmlFor="pageSize">Показувати по:</label>
                    <select
                        className="form-select"
                        name="pageSize"
                        value={pageSize}
                        onChange={(e) => setPageSize(e.target.value)}
                    >
                        <option value={6}>6</option>
                        <option value={12}>12</option>
                        <option value={24}>24</option>
                    </select>
                </div>
            </div>

            {subscribed.length < 1 ?
                <h3>Об'єкти моніторингу не обрані. Скористайтесь<Link className="icons" to="/search">Пошуком.</Link>
                </h3> :
                <div className="d-flex flex-wrap">{subscribed.map(person => {
                    return <Card key={person.id} data={person}/>;
                })}</div>}


            {totalFiles > pageSize &&
            <Pagination
                filesPerPage={pageSize}
                totalFiles={totalFiles}
                paginate={paginate}
            />}
        </div>

    );
};

export default Monitoring;
