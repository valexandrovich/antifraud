import React, { useEffect, useState } from "react";
import userApi from "../../api/UserApi";
import PreviewTableGroups from "../../common/Relations/PreviewTableGroups";
import PreviewTableNew from "../../common/Relations/PreviewTableNew";
import YPersonService from "../../api/YPersonApi";
import { useDispatch } from "react-redux";
import { setAlertMessageThunk } from "../../store/reducers/actions/Actions";

const Relations = () => {
  const dispatch = useDispatch();
  const [comparisonsPerson, setComparisonsPerson] = useState({
    newPeople: [],
    relationGroups: [],
  });
  const [relationType, setRelationType] = useState("");
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [checked, setChecked] = useState([]);
  const handleCheckboxChange = (e) => {
    let newArray = [...checked, e.target.id];
    if (checked.includes(e.target.id)) {
      newArray = newArray.filter((day) => day !== e.target.id);
    }
    setChecked(newArray);
  };

  const JoinToRelations = () => {
    YPersonService.joinToExistingRelation(selectedGroup, checked).then(
      (res) => {
        if (res.status > 300) {
          debugger;
          dispatch(
            setAlertMessageThunk(
              res.data.messages.map((m) => m),
              "danger"
            )
          );
        }
        dispatch(setAlertMessageThunk("Додано до зв'язків", "success"));
        setComparisonsPerson({
          newPeople: [],
          relationGroups: [],
        });
      }
    );
  };

  const createNew = () => {
    YPersonService.createNewRelation(checked, relationType).then((res) => {
      if (res.status > 300) {
        debugger;
        dispatch(
          setAlertMessageThunk(
            res.data.messages.map((m) => m),
            "danger"
          )
        );
      }
      dispatch(setAlertMessageThunk("Створено новий зв'язок", "success"));
      setComparisonsPerson({
        newPeople: [],
        relationGroups: [],
      });
    });
  };

  useEffect(() => {
    userApi.getComparisonsPerson().then((res) =>
      setComparisonsPerson({
        newPeople: res.newPeople,
        relationGroups: res.relationGroups,
      })
    );
  }, [comparisonsPerson.newPeople.length]);
  return (
    <div className="wrapped">
      <div className="card m-3 d-flex">
        <div className="card_header">
          <div className="text-center">
            <h3>Зв'язки</h3>
          </div>
          {comparisonsPerson.relationGroups?.map((el) => {
            return (
              <div key={el.group.id}>
                <PreviewTableGroups
                  name={`${el.group.id} ${el.group.relationType.type}`}
                  group={el}
                  type={"checkbox"}
                  setSelectedGroup={setSelectedGroup}
                />
              </div>
            );
          })}

          <PreviewTableNew
            name={"Нові"}
            check={handleCheckboxChange}
            people={comparisonsPerson.newPeople}
            setComparisonsPerson={setComparisonsPerson}
            type={"checkbox"}
            setSelectedGroup={setSelectedGroup}
          />

          <div className={"card-footer d-flex justify-content-between"}>
            <div className="form-group col-md-3">
              <span className="has-float-label">
                <select
                  onChange={(e) => setRelationType(e.target.value)}
                  name="relationType"
                  className="form-select"
                >
                  <option value="">Додати до існуючого</option>
                  <option value="1">Створити новий Sibling</option>
                  <option value="2">Створити новий Parent</option>
                  <option value="3">Створити новий Spouse</option>
                  <option value="4">Створити новий Alter ego</option>
                </select>
              </span>
            </div>
            {relationType === "" ? (
              <button
                disabled={selectedGroup == null || checked.length < 1}
                className={"btn custom-btn"}
                onClick={JoinToRelations}
              >
                Додати
              </button>
            ) : (
              <button
                disabled={checked.length < 2 || selectedGroup === ""}
                onClick={createNew}
                className={"btn custom-btn"}
              >
                Обеднати
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Relations;
