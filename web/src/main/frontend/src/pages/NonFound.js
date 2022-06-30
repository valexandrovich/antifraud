import React from "react";
import { useHistory } from "react-router-dom";

const NonFound = () => {
  const history = useHistory();

  return (
    <div className="wrapped">
      <div className="center">
        <div className="error">
          <div className="number">4</div>
          <div className="illustration">
            <div className="circle" />
            <div className="clip">
              <div className="paper">
                <div className="face">
                  <div className="eyes">
                    <div className="eye eye-left" />
                    <div className="eye eye-right" />
                  </div>
                  <div className="rosyCheeks rosyCheeks-left" />
                  <div className="rosyCheeks rosyCheeks-right" />
                  <div className="mouth" />
                </div>
              </div>
            </div>
          </div>
          <div className="number">4</div>
        </div>
        <h3 className="text">Дана сторінка відсутня</h3>
        <button onClick={() => history.goBack()} className="button">
          Повернутися
        </button>
      </div>
    </div>
  );
};

export default NonFound;
