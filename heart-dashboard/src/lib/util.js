export const isNil = (value) => value === null || value === undefined;
export const isDefined = (value) => !isNil(value);

export const clsx = (...args) =>
  args
    .flat()
    .filter((x) => x !== null && x !== undefined && typeof x !== "boolean")
    .join(" ");
