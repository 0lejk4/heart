import React from "react";
import "./styles.css";

const options = [
  { value: "1000", label: "1 Second" },
  { value: "5000", label: "5 Seconds" },
  { value: "30000", label: "30 Seconds" },
  { value: "60000", label: "1 Minute" },
  { value: "300000", label: "5 Minute" },
];

const PollButton = React.forwardRef((props, ref) => {
  return (
    <select className="heart-poll-button" ref={ref} {...props}>
      <option value="">Poll interval</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
});

export default PollButton;
