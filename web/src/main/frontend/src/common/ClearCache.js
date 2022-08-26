import React, { useState, useEffect } from "react";
import moment from "moment";

export const getBuildDate = (epoch) => {
  return new Date(epoch);
};

const buildDateGreaterThan = (latestDate, currentDate) => {
  const momLatestDateTime = moment(latestDate);
  const momCurrentDateTime = moment(currentDate);
  return momLatestDateTime.isAfter(momCurrentDateTime);
};

function withClearCache(Component) {
  function ClearCacheComponent(props) {
    const [isLatestBuildDate, setIsLatestBuildDate] = useState(false);

    useEffect(() => {
      fetch("./meta.json")
        .then((response) => response.json())
        .then((meta) => {
          const latestVersionDate = meta.buildDate;
          const currentVersionDate = Math.floor(Date.now() / 1000);
          const shouldForceRefresh = buildDateGreaterThan(
            latestVersionDate,
            currentVersionDate
          );
          if (shouldForceRefresh) {
            setIsLatestBuildDate(false);
            refreshCacheAndReload();
          } else {
            setIsLatestBuildDate(true);
          }
        });
    }, []);

    const refreshCacheAndReload = () => {
      const location = window.location.href;
      window.location.replace(location);
      setIsLatestBuildDate(true);
    };
    return <>{isLatestBuildDate ? <Component {...props} /> : null}</>;
  }

  return ClearCacheComponent;
}

export default withClearCache;
