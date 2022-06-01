import React from "react";
import csvSettings from "../Csv/CsvSettings";

import Modal from "./Modal";

const CsvModal = ({open, onClose, csvoptions, setCsvOptions}) => {
    const handleChange = (e) => {
        const {name, value} = e.target;
        setCsvOptions({
            ...csvoptions,
            [name]: value
        });
    };

    return (<Modal
        title={`Налаштування файлу`}
        open={open}
        onClose={onClose}
    >
        <div className="modal-content">
            <div className="modal-body">
                <div className="row">
                    <div className="form-group col-md-4 mb-2">
                        <label htmlFor="delimeter">Символ розділення</label>
                        <select
                            className="form-select"
                            name="delimeter"
                            value={csvoptions.delimeter}
                            onChange={handleChange}
                        >
                            {csvSettings.delimeter.map(({id, name}) => {
                                return (<option value={name} key={id}>{name}</option>);
                            })}
                        </select>
                    </div>
                    <div className="form-group col-md-4 mb-2">
                        <label htmlFor="codingType">Формат кодування</label>
                        <select
                            className="form-select"
                            name="codingType"
                            value={csvoptions.codingType}
                            onChange={handleChange}
                        >
                            {csvSettings.codingType.map(({id, name}) => {
                                return (<option value={name} key={id}>{name}</option>);
                            })}
                        </select>
                    </div>
                </div>
            </div>
            <div className="modal-footer mt-3">
                <button
                    onClick={onClose}
                    className="btn custom-btn"
                    type="submit"
                >
                    Завантажити
                </button>
            </div>
        </div>
    </Modal>);


};

export default CsvModal;