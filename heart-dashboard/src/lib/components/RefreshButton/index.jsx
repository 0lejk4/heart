import React from "react";
import "./styles.css";

const RefreshButton = React.forwardRef((props, ref) => (
  <button className="heart-refresh-button" ref={ref} {...props}>
    Refresh
  </button>
));

export default RefreshButton;
