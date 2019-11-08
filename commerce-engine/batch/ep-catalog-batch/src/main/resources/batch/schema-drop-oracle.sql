CREATE OR REPLACE PROCEDURE drop_table (table_name IN varchar2) IS
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE ' || table_name;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE <> -942 THEN
        RAISE;
      END IF;
END drop_table;
/

CREATE OR REPLACE PROCEDURE drop_sequence (sequence_name IN varchar) IS
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE ' || sequence_name;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE <> -2289 THEN
        RAISE;
      END IF;
END drop_sequence;
/

BEGIN
  drop_table ( 'BATCH_STEP_EXECUTION_CONTEXT' );
  drop_table ( 'BATCH_JOB_EXECUTION_CONTEXT' );
  drop_table ( 'BATCH_STEP_EXECUTION' );
  drop_table ( 'BATCH_JOB_EXECUTION_PARAMS' );
  drop_table ( 'BATCH_JOB_EXECUTION' );
  drop_table ( 'BATCH_JOB_INSTANCE' );
  drop_sequence ( 'BATCH_STEP_EXECUTION_SEQ' );
  drop_sequence ( 'BATCH_JOB_EXECUTION_SEQ' );
  drop_sequence ( 'BATCH_JOB_SEQ' );
END;
/

DROP PROCEDURE drop_table
/

DROP PROCEDURE drop_sequence
/
