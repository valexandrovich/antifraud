import React, { useState, useEffect } from "react";
import packageJson from "../../package.json";
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
          const currentVersionDate = packageJson.buildDate;
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
      if (caches) {
        caches.keys().then((names) => {
          for (const name of names) {
            caches.delete(name);
          }
        });
      }

      const location = window.location.href;
      window.location.replace(location);
      setIsLatestBuildDate(true);
    };
    return <>{isLatestBuildDate ? <Component {...props} /> : null}</>;
  }

  return ClearCacheComponent;
}

export default withClearCache;
