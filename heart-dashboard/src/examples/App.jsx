import React from "react";
import HealthDashboard from "../lib/components/HealthDashboard";

const config = {
  name: "Scala Server",
  url: "http://localhost:8080",
  // auth: { type: "Basic" },
  // auth: { type: "Basic", username: "secret" },
  // auth: { type: 'Basic', username: 'secret', password: 'secret' },
  // auth: { type: "Secret", secret: "secret" },
  // auth: { type: 'Secret', secret: 2 },
};

function App() {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        paddingTop: "24px",
      }}
    >
      <HealthDashboard {...config} />
    </div>
  );
}

export default App;
