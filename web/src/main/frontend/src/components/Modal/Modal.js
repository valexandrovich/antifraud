import React, {useEffect, useCallback, useRef} from "react";
import "./Modal.css";

const Modal = ({open, onClose, title, children}) => {
    const modalRef = useRef();

    const closeModal = (e) => {
        if (modalRef.current === e.target) {
            onClose();
        }
    };

    const keyPress = useCallback(
        (e) => {
            if (e.key === "Escape") {
                onClose();
            }
        },
        [onClose]
    );

    useEffect(() => {
        document.addEventListener("keydown", keyPress);
        return () => document.removeEventListener("keydown", keyPress);
    }, [keyPress]);

    if (!open) {
        return null;
    }

    return (
        <div className="modal-bg" onClick={closeModal}>
            <div className="modal-wrapper">
                <div className="modal-header">
                    <h4>{title}</h4>
                    <button
                        onClick={onClose}
                        type="button"
                        className="btn-close "
                    ></button>
                </div>
                <div className="modal-content">{children}</div>
            </div>
        </div>
    );
};
export default Modal;
