CREATE TABLE IF NOT EXISTS "cucumber_performance_results"
(
   id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
   jenkins_job_id integer,
   date_executed timestamp with time zone NOT NULL,
   epc_version varchar(20) NOT NULL,
   application varchar(20) NOT NULL,
   cuke_scenario varchar(100) NOT NULL,
   total_db_selects integer NOT NULL,
   total_db_inserts integer NOT NULL,
   total_db_updates integer NOT NULL,
   total_db_deletes integer NOT NULL,
   total_db_time integer NOT NULL,
   commit_hash varchar(100)
);

