import React from "react";
import ReportDuration from "../ReportDuration";
import ReportStatus from "../ReportStatus";
import ReportTags from "../ReportTags";
import "./styles.css";
import { isDefined } from "../../util";
import ReportComponent from "../ReportComponent";
import ReportMessage from "../ReportMessage";
import ReportExtra from "../ReportExtra";

const fields = [
  {
    name: "Component",
    render: (h) => <ReportComponent component={h.component} />,
  },
  {
    name: "Status",
    render: (h) => <ReportStatus status={h.status} />,
  },
  {
    name: "Message",
    render: (h) => <ReportMessage message={h.message} />,
  },
  {
    name: "Tags",
    render: (__, c) => <ReportTags tags={c.tags} color="stone" />,
  },
  {
    name: "Duration",
    render: (h) => <ReportDuration duration={h.duration} />,
  },
  {
    name: "Extra",
    render: (h) => <ReportExtra extra={h.extra} />,
  },
];

const HealthTable = ({ healthChecks, components }) => {
  const error = (healthChecks.error ?? components.error)?.message;
  return (
    <div className="heart-datagrid-wrapper">
      <table className="heart-datagrid">
        <thead>
          <tr className="heart-datagrid-header">
            {fields.map((f) => (
              <th key={f.name} className="heart-datagrid-cell">
                {f.name}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {isDefined(healthChecks.data) &&
            isDefined(components.data) &&
            healthChecks.data.components.map((h) => (
              <tr key={h.component} className="heart-datagrid-row">
                {fields.map((f) => (
                  <td key={f.name} className="heart-datagrid-cell">
                    {f.render(
                      h,
                      components.data.find((c) => c.name === h.component)
                    )}
                  </td>
                ))}
              </tr>
            ))}
          {(healthChecks.loading || components.loading) && (
            <tr>
              <td>
                <h2>Loading...</h2>
              </td>
            </tr>
          )}
          {isDefined(error) && (
            <tr>
              <td>
                <h2>{error}</h2>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default HealthTable;
