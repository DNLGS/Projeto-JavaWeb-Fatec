# Locadora Rebobina CRUD execution progress

- Plan revision — 2
- Starting branch and commit — `main` at `2e282b58549903fe2fc82581f0a3398e95c34768`
- Starting local changes relevant to the plan — None before authoring these documents.
- Active or next task — None
- Next action — None

## Task status

| Task | Status | Validation or blocker |
| --- | --- | --- |
| T01 | completed | Clean Ant WAR build. Isolated HTTP checks passed for the home page, film creation and film search. |
| T02 | completed | Ant WAR and isolated film smoke passed. Direct JSP access returned 404. |
| T03 | completed | Ant WAR and isolated address smoke passed, including leading zeros and conflicts for both person types. |
| T04 | completed | Ant WAR and isolated client smoke passed, including duplicate rollback, a legacy address-less row and rental conflict. |
| T05 | completed | Ant WAR and isolated employee smoke passed, including validation, legacy date and address retention. |
| T06 | completed | Ant WAR and isolated rental smoke passed, including multiple films, rollback and historical dates. |
| T07 | completed | Final Ant WAR, all five isolated HTTP smoke tests, navigation and legacy redirects passed. |

## Execution records

- Planning baseline — Inspected the tracked Java, HTML, SQL and build paths. No application files changed.
- T01 preflight — Ran `ant -f build-ant.xml war` from the repository root. PowerShell returned exit 1 because it could not resolve `ant`. No Java compilation or WAR build ran. `java -version` confirmed JDK 21. `mysql --version` reported MySQL client 8.4.9. The loopback port 3306 probe succeeded. No database login, schema import or HTTP test was attempted. Resume by completing the isolated setup and running the exact T01 validation in the plan.

- T01 execution — Built the unchanged WAR with JDK 21, Ant 1.10.18 and Tomcat 10.1.62. A sandboxed attempt printed a JAR access error, so the build was rerun outside the sandbox and completed cleanly. Deployed the WAR in Docker under WSL with MySQL 8.4.13, a shared isolated loopback and no published database ports. After correcting fixture configuration, the index returned HTTP 200, the film POST returned HTTP 302 and search found exactly one matching row. The Windows MySQL instance was not used.

- T02 execution — Ant rebuilt all 18 Java sources and packaged the WAR. The PowerShell smoke test passed in Docker against Tomcat and MySQL, covering the film lifecycle, escaped title, invalid form, CSRF and stale writes. The form JSP rendered over HTTP and direct access under WEB-INF returned 404. `git diff --check` passed.

- T03 execution — Rebuilt the WAR with Ant and ran the address smoke in the isolated Tomcat and MySQL environment. Create, search, prefill, edit, confirmation and delete passed. Leading zeros survived in list and form. Deleting an address linked to a client or employee returned 409 and kept both records.

- T04 execution — Ant compiled and packaged the WAR. The first client smoke run failed the prefill text assertion. Declared UTF-8 in the shared JSP fragments and reran the unchanged assertions successfully. Verified that duplicates did not leave stray addresses, legacy dates survived editing, an address could be added, and deletion of a rented-to client returned 409.

- T05 execution — Ant rebuilt the WAR. Employee smoke passed through Tomcat and MySQL for CRUD, cargo and level changes, rejected negative level, duplicate rollback, historical registration date and optional address for a legacy record. Employee deletion retained its address.

- T06 execution — Ant rebuilt the WAR. Rental smoke passed for CRUD, search by one film with all films displayed, rejected repeated IDs, FK rollback, linked film restriction, association cleanup, and historical return date save and clear.
- Revision 2 — T07 includes updating test fixtures that used the soon-to-be-retired route. Product acceptance and completed task IDs are unchanged.
- T07 execution — Final Ant WAR built with 21 Java sources. The isolated Tomcat and MySQL run passed all five PowerShell CRUD smoke tests, confirmed every menu link and legacy HTML redirect over HTTP, and verified HTTP 405 for legacy page POSTs and HTTP 404 for the retired endpoint and direct JSP access. The source scan found HTML forms only in JSPs and no legacy servlet or servlet-side markup. `git diff --check` and PowerShell parser checks passed. Pages were checked through HTTP responses. No interactive browser visual review or production deployment was performed.

## Approved decisions

- Revision 1, T02–T07 — The original request specifies one servlet per model, presentation in JSPs, completion of edit and delete, and preservation of the existing simple visual style. Repository evidence identifies five independently persisted models and `Pessoa` as common model state.
- Revision 1, T03–T05 and T07 — The user selected standalone address CRUD with a menu entry and continued integrated address creation in the person forms.

## Execution instructions

1. Before starting or resuming authorized work, read `PLAN.md` and `PROGRESS.md`. Verify the revisions match, check the active task's prerequisites and compare the relevant checkout state with the plan and recorded progress. On resumption, inspect any partial work before editing.
2. Follow plan order and work on one task at a time. Mark it `in progress` before editing. Limit edits to listed files and constraints. Stop and obtain approval before changing the architecture or expanding scope.
3. Run the specified validation, check every acceptance criterion and save the actual results here. Mark the task `completed` only after all criteria pass. Save the next task and action before starting it. Automatically continue to the next task with satisfied prerequisites. When all tasks are complete, set both fields to `None`.
4. Stop at the first failed or unavailable check, unmet prerequisite or material difference. Mark the affected task `blocked` and record the evidence, partial work and condition for resuming. Leave subsequent pending tasks untouched. Resume the same task when the blocker is resolved and work is authorized. Revalidate before marking it complete.
