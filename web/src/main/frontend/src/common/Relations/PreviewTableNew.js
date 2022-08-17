import React from "react";
import ConfirmDeletemodal from "../../components/Modal/ConfirmDeletemodal";
import { useDispatch } from "react-redux";
import { useState } from "react";
import userApi from "../../api/UserApi";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";

const PreviewTableNew = ({
  name,
  type,
  people,
  setComparisonsPerson,
  check,
}) => {
  const dispatch = useDispatch();
  const [confirmationRemove, setConfirmationRemove] = useState(null);
  const deleteAction = (id) => {
    userApi.unComparePerson(id).then((res) => {
      if (res.status > 300) {
        dispatch(setAlertMessageThunk("Виникла помилка", "danger"));
        setConfirmationRemove(null);
      } else {
        setComparisonsPerson((prevState) => ({
          ...prevState,
          newPeople: prevState.newPeople.filter((el) => el.id !== id),
        }));
        setConfirmationRemove(null);
      }
    });
  };
  return (
    <div className="card-body">
      <h1>{name}</h1>
      <table className="table table-striped table-bordered table-sm">
        <thead>
          <tr>
            <th></th>
            <th>Ім'я</th>
            <th>Дата народження</th>
            <th>Паспорт</th>
            <th>ІПН</th>
            <th>Адреса</th>
            <th>Дії</th>
          </tr>
        </thead>
        <tbody>
          {people.map((data) => {
            return (
              <tr key={data.id}>
                <td className="text-center">
                  <div className="radio">
                    <label>
                      <input
                        onChange={check}
                        id={data.id}
                        className={"big-checkbox"}
                        type={type}
                        name={name}
                      />
                    </label>
                  </div>
                </td>

                <td>
                  {data.lastName} {data.firstName} {data.patName}
                </td>
                <td>{data.birthdate}</td>
                <td>
                  {data.passport?.series}
                  {data.passport?.number}
                </td>
                <td>{data.inn?.inn}</td>
                <td>{data.address?.address}</td>
                <td>
                  <button
                    style={{ float: "right" }}
                    onClick={() => setConfirmationRemove(data.id)}
                    className={"btn btn-danger"}
                  >
                    Видалити
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
      {confirmationRemove && (
        <ConfirmDeletemodal
          open
          onClose={() => setConfirmationRemove(null)}
          uuid={confirmationRemove}
          deleteAction={deleteAction}
        />
      )}
    </div>
  );
};

export default PreviewTableNew;
