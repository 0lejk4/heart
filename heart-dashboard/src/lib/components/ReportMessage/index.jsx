import React from "react";
import { isNil } from "../../util";
import "./styles.css";

function ReportMessage({ message }) {
  if (isNil(message)) return <span>—</span>;

  return (
    <div className="heart-report-message__container">
      <span className="heart-report-message">{message}</span>
    </div>
  );
}

export default ReportMessage;
