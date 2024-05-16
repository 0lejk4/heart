import React from "react";
import Tag from "../ReportTag";
import "./styles.css";

function TagList({ tags, color }) {
  return (
    <div className="heart-tag-list">
      {tags.map((tag) => (
        <Tag key={tag} color={color ?? "green"}>
          {tag}
        </Tag>
      ))}
    </div>
  );
}

export default TagList;
