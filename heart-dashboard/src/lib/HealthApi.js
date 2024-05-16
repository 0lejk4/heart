import { useCallback, useMemo, useState } from "react";

export function fetchHeart(url, auth) {
  const headers = {};
  if (auth.type === "Basic")
    headers.Authorization =
      "Basic " + btoa(auth.username + ":" + auth.password);
  if (auth.type === "Secret") headers.Authorization = "Secret " + auth.secret;
  return fetch(url, { headers }).then(
    (response) => {
      if (response.status === 401) return { type: "auth" };
      return response.json().then((data) => ({ type: "data", data }));
    },
    (error) => {
      // console.log(reason);
      return { type: "error", error };
    }
  );
}

export function useHealthApi() {
  const [auth, setAuth] = useState(null);
  const setPublicAuth = useCallback(
    () => setAuth({ type: "Public" }),
    [setAuth]
  );
  const setBasicAuth = useCallback(
    (username, password) => setAuth({ type: "Basic", username, password }),
    [setAuth]
  );
  const setSecretAuth = useCallback(
    (secret) => setAuth({ type: "Secret", secret }),
    [setAuth]
  );

  const api = useMemo(
    () => ({
      //AUTH
      auth,
      setPublicAuth,
      setBasicAuth,
      setSecretAuth,
      isAuthed: auth !== null,
    }),
    [auth, setBasicAuth, setSecretAuth, setPublicAuth]
  );

  return api;
}
