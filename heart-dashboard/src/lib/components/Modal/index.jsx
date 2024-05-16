import React, { useEffect } from "react";
import Portal from "../Portal";
import "./styles.css";

function Modal({ children, isOpen, onClose, id }) {
  useEffect(() => {
    const closeOnEscapeKey = (e) => (e.key === "Escape" ? onClose() : null);
    document.body.addEventListener("keydown", closeOnEscapeKey);
    return () => {
      document.body.removeEventListener("keydown", closeOnEscapeKey);
    };
  }, [onClose]);

  if (!isOpen) return null;

  return (
    <Portal wrapperId={id}>
      <div className="heart-modal">
        <div className="heart-modal__content-wrapper">
          <div className="heart-modal__close-btn-wrapper">
            <button onClick={onClose} className="heart-modal__close-btn">
              Close
            </button>
          </div>
          <div className="heart-modal__content">{children}</div>
        </div>
      </div>
    </Portal>
  );
}

export default Modal;
