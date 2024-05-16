import React from 'react';
import { HealthView } from 'heart-admin-components';
import style from './App.css';

const config = {
  name: 'My App',
  url: 'http://localhost:8080',
  // auth: { type: "Basic" },
  // auth: { type: "Basic", username: "secret" },
  // auth: { type: 'Basic', username: 'secret', password: 'secret' },
  // auth: { type: "Secret", secret: "secret" },
  // auth: { type: 'Secret' },
};

function App() {
  return (
    <div className={style.app}>
      <HealthView config={config} />
    </div>
  );
}

export default App;
