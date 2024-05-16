import PropTypes from "prop-types";
import React, { useCallback, useEffect, useState } from "react";
import { fetchHeart, useHealthApi } from "../../HealthApi";
import { isDefined, isNil } from "../../util";
import HealthTable from "../HealthTable";
import PollButton from "../PollButton";
import RefreshButton from "../RefreshButton";
import ReportDuration from "../ReportDuration";
import ReportStatus from "../ReportStatus";
import "./styles.css";

const HealthDashboard = ({ url, name, auth }) => {
  const [pollInterval, setPollInterval] = React.useState("");
  const api = useHealthApi(url);
  const components = useHealthApiCall(url + "/health/components", api.auth);
  const healthChecks = useHealthApiCall(url + "/health/report", api.auth);
  const status = healthChecks.data?.status;
  const totalDuration = healthChecks.data?.totalDuration;
  const refreshComponents = components.refresh;
  const refreshHealthChecks = healthChecks.refresh;

  //Poll
  useEffect(() => {
    if (pollInterval === "") return;

    const interval = setInterval(() => {
      refreshComponents();
      refreshHealthChecks();
    }, parseInt(pollInterval));

    return () => clearInterval(interval);
  }, [pollInterval, refreshComponents, refreshHealthChecks]);

  //Login
  useEffect(() => {
    if (isNil(api.auth)) login(api, auth);
  }, [api, auth]);

  return (
    <div className="heart-dashboard">
      <div className="heart-dashboard__header">
        <div className="heart-title">
          {isNil(name) ? (
            <div />
          ) : (
            <h2 className="heart-title__name">{name}</h2>
          )}
          {status && <ReportStatus status={status} />}
          {totalDuration && (
            <ReportDuration
              duration={totalDuration}
              className="heart-title__duration"
            />
          )}
        </div>
        <div className="heart-dashboard__buttons">
          <PollButton
            value={pollInterval}
            onChange={(e) => setPollInterval(e.target.value)}
          />
          <RefreshButton
            onClick={() => {
              healthChecks.refresh();
              components.refresh();
            }}
          />
        </div>
      </div>
      <HealthTable healthChecks={healthChecks} components={components} />
    </div>
  );
};

HealthDashboard.propTypes = {
  url: PropTypes.string.isRequired,
  name: PropTypes.string,
  auth: PropTypes.oneOfType([
    PropTypes.exact({
      type: PropTypes.oneOf(["Basic"]).isRequired,
      username: PropTypes.string,
      password: PropTypes.string,
    }),
    PropTypes.exact({
      type: PropTypes.oneOf(["Secret"]).isRequired,
      secret: PropTypes.string,
    }),
    PropTypes.exact({
      type: PropTypes.oneOf(["Public"]).isRequired,
    }),
  ]),
};

function login(api, auth) {
  if (auth?.authType === "Basic") {
    const username = auth.username ?? prompt("Username:");
    const password = auth.password ?? prompt("Password:");
    api.setBasicAuth(username, password);
  } else if (auth?.authType === "Secret") {
    const secret = auth.secret ?? prompt("Secret:");
    api.setSecretAuth(secret);
  } else {
    api.setPublicAuth();
  }
}

function useHealthApiCall(url, auth) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState();
  const [data, setData] = useState();
  const [refreshCount, setRefreshCount] = useState(0);
  const refresh = useCallback(
    () => setRefreshCount((c) => c + 1),
    [setRefreshCount]
  );

  useEffect(() => {
    if (isDefined(auth)) {
      fetchHeart(url, auth).then((res) => {
        setLoading(false);
        if (res.type === "data") setData(res.data);
        else if (res.type === "auth") setError(res);
        else if (res.type === "error") setError(res.error);
      });
    }
  }, [url, auth, refreshCount]);

  return {
    data,
    loading,
    error,
    refresh,
  };
}

export default HealthDashboard;
