import React from "react";

const leadingZero = (time) => (time < 10 ? "0" + time : time);

export function nanosToDetailedTime(nanos) {
  const ms = nanos / 1000000;
  const milliseconds = Math.floor((ms % 1000) / 100);
  const seconds = Math.floor((ms / 1000) % 60);
  const minutes = Math.floor((ms / (1000 * 60)) % 60);
  const hours = Math.floor((ms / (1000 * 60 * 60)) % 24);
  return (
    [hours, minutes, seconds].map(leadingZero).join(":") + "." + milliseconds
  );
}

export function nanosToClosestTime(nanos) {
  const ms = nanos / 1000000;
  const seconds = (ms / 1000).toFixed(4);
  const minutes = (ms / (1000 * 60)).toFixed(4);
  const hours = (ms / (1000 * 60 * 60)).toFixed(4);
  if (seconds < 60) return seconds + " Seconds";
  else if (minutes < 60) return minutes + " Minutes";
  return hours + " Hours";
}

const ReportDuration = React.forwardRef(
  ({ duration, detailed, ...props }, ref) => {
    const format = detailed ? nanosToDetailedTime : nanosToClosestTime;
    return (
      <span ref={ref} {...props}>
        {format(duration)}
      </span>
    );
  }
);

export default ReportDuration;
