SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE station RESTART IDENTITY;
TRUNCATE TABLE line RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;