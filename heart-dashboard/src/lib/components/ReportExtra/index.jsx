import React from "react";
import { isNil } from "../../util";
import Modal from "../Modal";
import "./styles.css";
import { ReactComponent as LoopIcon } from "../../loop-icon.svg";
import { ReactComponent as ViewIcon } from "../../view-icon.svg";

function ReportExtra({ extra }) {
  const [isOpen, setIsOpen] = React.useState(false);

  if (isNil(extra) || extra === {}) <span>—</span>;

  return (
    <div>
      <button className="heart-extra__view-btn" onClick={() => setIsOpen(true)}>
        {/* <LoopIcon /> */}
        <ViewIcon />
      </button>

      <Modal onClose={() => setIsOpen(false)} isOpen={isOpen} id="extra">
        <pre className="heart-extra__json">
          {JSON.stringify(extra, null, 4)}
        </pre>
      </Modal>
    </div>
  );
}

export default ReportExtra;
