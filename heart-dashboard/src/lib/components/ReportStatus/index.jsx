import React from "react";
import Tag from "../ReportTag";

function HealthStatus({ status }) {
  return <Tag color={StatusToColor[status]}>{status}</Tag>;
}

const StatusToColor = {
  Healthy: "green",
  Degraded: "red",
  Unhealthy: "yellow",
};

export default HealthStatus;
