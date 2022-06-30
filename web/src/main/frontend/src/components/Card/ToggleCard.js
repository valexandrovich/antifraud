import React from "react";
import * as IoIcons from "react-icons/io";

const ToggleCard = ({
  element,
  setComponents,
  components,
  type,
  name,
  children,
}) => {
  return (
    <>
      {element && element.length > 0 && (
        <div className="pb-2">
          <div
            onClick={() => {
              setComponents((prevState) => ({
                ...prevState,
                [type]: !prevState[type],
              }));
            }}
            className="d-flex justify-content-between pointer"
          >
            <span className={components ? "mb-2" : null}>{name}</span>
            <span>
              {components ? (
                <IoIcons.IoIosArrowUp />
              ) : (
                <IoIcons.IoIosArrowDown />
              )}
            </span>
          </div>
          <div>{components && children}</div>
          <hr />
        </div>
      )}
    </>
  );
};

export default ToggleCard;
