#Batch Jobs

This module contains a collection of batch jobs which are executed at different time intervals by the Quartz framework.

For the full list of jobs, [see EP docs](https://documentation.elasticpath.com/commerce/docs/core/platform/cross-platform/quartz.html#elastic-path-quartz-jobs).

The majority of the batch jobs lives in this module, and the rest can be found in the **ep-core** module.

####The folder structure and file organization

**src/java/com/elasticpath/batch/jobs/impl**
Contains job implementations

**src/resources/META-INF**
Contains persistence-related files. All batch-job related queries can be found in per-job ORM files.  
See also `shared-queries-orm.xml` in `ep-core` module.

**src/resources/spring**
Contains Spring job, Quartz and data persistence definitions

####How to write a new job

New batch jobs must extend **com.elasticpath.batch.jobs.AbstractBatchJob** and **com.elasticpath.batch.jobs.AbstractBatchProcessor** classes.
Please, refer to javadocs for more information.

####How to test

All batch jobs are triggered by Quartz framework. This may not be convenient during development (especially debugging), and the more controllable way
is required. The following steps describe how to do that:

* Add depencency on **ep-batch** module in **ep-health-monitoring** - replace **ref="allBatchTriggers"** with **ref="disabled" **
* Disable Quartz triggers in **ep-batch/src/main/filtered-resources/spring/scheduling/quartz-setup.xml**
* Edit `StatusCheckerServlet.java`

    
    ApplicationContext context;
    
    InactiveCartsCleanupJob inactiveCartsCleanupJob;
 
    @Override
    
    public void init() throws ServletException {
        ...
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext()); //NEW
        ...
        inactiveCartsCleanupJob = context.getBean("inactiveCartsCleanupJob", InactiveCartsCleanupJob.class); //NEW
        ...
     }
     
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
         
        if (request.getParameter("newJob") != null) {
            myNewJob.execute();
        } else if (request.getParameter("oldJob") != null) {
            oldJob.oldJobMethod();
        }
         //getServerStatusChecker().getServerStatus(refreshIntervalSeconds, statusChecker, request, response);
     }

This approach enables the testing of one or more jobs (new and old ones), by calling corresponding job methods.

* Build **ep-health-monitoring** and **ext-batch-webapp** modules
* Deploy batch server and run Tomcat

* Trigger the job via `http://localhost:<BATCH_SERVER_PORT>/batch/status/?newJob=1` (or oldJob=1 to trigger the old job).

IMPORTANT: Upon test completion, all changes must be reverted.

####Performance considerations

* The JPQL DELETE should be normally translated into a native DELETE statement, but if EntityManager contains one or more lifecycle listeners, the
OpenJPA framework will fetch the whole graph before deleting the instance. This is undocumented behavior (at the time of writing 2.4.0 docs) and has
a profound negative effect on performance.  
This is fixed by resetting all listeners in the `serviceBatch.xml` by using `<util:list id="entityManagerLifecycleListeners"/>`.

* JPQL UPDATE with sub-queries  
  The JPQL UPDATE queries like `UPDATE Entity1 e1 WHERE e1.field IN (SELECT e2.field FROM Entity e2)` have a profound negative effect on performance
  and should be avoided at all cost. The effect is emphasized on tables with 10s/100s Ks of records. The database performs a full table scan even if
  all WHERE fields are indexed. This is seen in MySQL 5.7 and may be specific to this db only. This is not an OpenJPA issue.  
  The correct alternative is to retrieve fields in a separate db call and then perform the update.  
  See **PurgeCartsBatchProcessor**.
  
* Verify all queries using [DB QA tool](https://documentation.elasticpath.com/commerce/docs/tools/query-analyzer/index.html]).  
  Verify both, old and new, jobs in order to understand the improvements/regression introduced with new implementation.

* Verify all queries using a big set of data (10s/100s Ks of records)

* Use DB CASCADE on DELETE whenever possible because it will reduce the number of DELETE DMLs and make cleanup more effective
  See `core-changelog-2020-05-cart-cleanup-jobs.xml`. It may be required write a Liquibase extension class (`liquibase/liquibase-extensions`) for 
  performing additional data migration/cleanup tasks before applying db changes.
