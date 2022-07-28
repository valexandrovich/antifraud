import React from "react";
import Modal from "./Modal";

const ConfirmDeletemodal = ({ open, onClose, uuid, deleteAction }) => {
  return (
    <Modal
      title={`Запис з id: ${uuid} буде видалено`}
      open={open}
      onClose={onClose}
    >
      <div className="modal-content">
        <div className="modal-body">
          <button
            onClick={() => deleteAction(uuid)}
            className="btn btn-danger w-100"
          >
            Підтвердити
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmDeletemodal;
