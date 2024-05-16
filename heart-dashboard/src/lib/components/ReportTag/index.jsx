import React from "react";
import "./styles.css";

const Tag = React.forwardRef(({ color, children, ...props }, ref) => (
  <div ref={ref} className={`heart-tag heart-tag-${color}`} {...props}>
    {children}
  </div>
));

export default Tag;
