import React, { useState } from "react";
import userApi from "../../api/UserApi";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";
import { useDispatch } from "react-redux";
import ConfirmDeletemodal from "../../components/Modal/ConfirmDeletemodal";

const PreviewTableGroups = ({
  name,
  group,
  setSelectedGroup,
  delBTN,
  groupID,
  setComparisonsPerson,
}) => {
  const dispatch = useDispatch();
  const [confirmationRemove, setConfirmationRemove] = useState(null);
  const deleteAction = (personid, groupid) => {
    userApi.removePersonFromRelation(personid, groupid).then((res) => {
      if (res.status > 300) {
        dispatch(setAlertMessageThunk("Виникла помилка", "danger"));
        setConfirmationRemove(null);
      } else {
        setComparisonsPerson({
          newPeople: [],
          relationGroups: [],
        });
        setConfirmationRemove(null);
      }
    });
  };

  return (
    <div className="card-body">
      <div className={"d-flex align-items-center"}>
        <input
          onClick={(e) => setSelectedGroup(e.currentTarget.value)}
          className={"big-checkbox mr-10"}
          value={group.group.id}
          type={"radio"}
          name={"radio"}
        />
        <h1>{name}</h1>
      </div>

      <table className="table table-striped table-bordered table-sm">
        <thead>
          <tr>
            <th>Ім'я</th>
            <th>Дата народження</th>
            <th>Паспорт</th>
            <th>ІПН</th>
            <th>Адреса</th>
            <th>Дія</th>
          </tr>
        </thead>
        <tbody>
          {group?.people?.map((people) => {
            return (
              <tr key={people.id}>
                <td>
                  {people.lastName} {people.firstName} {people.patName}
                </td>
                <td>{people.birthdate}</td>
                <td>
                  {people.passport?.series}
                  {people.passport?.number}
                </td>
                <td>{people.inn?.inn}</td>
                <td>{people.address?.address}</td>
                <td>
                  {delBTN.map(({ id }) => {
                    if (id === people.id) {
                      return (
                        <button
                          onClick={() => setConfirmationRemove(people.id)}
                          style={{ float: "right" }}
                          key={people.id}
                          className={"btn btn-danger"}
                        >
                          Видалити
                        </button>
                      );
                    }
                    return "";
                  })}
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
          deleteAction={() => deleteAction(confirmationRemove, groupID)}
        />
      )}
    </div>
  );
};
export default PreviewTableGroups;
