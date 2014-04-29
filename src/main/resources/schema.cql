CREATE TABLE login (
  user text,
  ts timeuuid,
  area text,
  PRIMARY KEY (user, ts)
) WITH
  CLUSTERING ORDER BY (ts DESC);

CREATE TABLE loginStats (
  user text,
  date text,
  count counter,
  PRIMARY KEY (user, date)
);
