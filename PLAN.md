# Locadora Rebobina CRUD implementation plan

- Revision — 2
- Readiness — ready. The baseline WAR and disposable runtime passed T01.

## Outcome and scope

- T02–T06 deliver one servlet and full create, list/search, edit and delete flows for each persisted model: `Filme`, `Endereco`, `Cliente`, `Funcionario` and `Locacao`.
- JSPs own all HTML, including lists, forms, confirmation and errors. Keep the current plain HTML appearance, Brazilian Portuguese labels, navigation and accessibility basics. T07 completes the menu and removes the old paths.
- Preserve the current database schema, integrated address creation, search behavior and Java/JDBC stack. Keep authentication, new rental business rules, visual redesign and deployment changes outside scope.

## Current behavior

- Baseline is `main` at `2e282b58549903fe2fc82581f0a3398e95c34768`. `web/index.html` links to four static forms. `web/enderecos.html` is an informational page. No JSPs or tracked tests were found.
- `src/java/ClassServletBase.java` handles `/ClassServletBase`. Its POST dispatches by `tipo`, validates input and inserts. Its GET already searches and lists four entities using JDBC directly, and builds the tables, menu and errors with `PrintWriter`. `pagina` rejects standalone addresses.
- `src/java/database/BaseDao.java` provides `insert`, `update` and `delete` using prepared statements. Each concrete DAO already supplies write SQL. Update and delete discard the affected-row count. No DAO exposes list or lookup by ID.
- `ClienteDao` and `FuncionarioDao` extend `PessoaDao`. `Pessoa` holds common fields, has no table or independent ID, and is not created by an application route. `locacao_filme` has no standalone Java model. `PessoaDao.preparePessoa` currently binds the registration date on both writes. `Pessoa` and `Locacao` initialize that date to today, so reusing insert binding for update would overwrite it.
- `ClassServletBase.salvarPessoa` inserts the address and person in a single transaction. `LocacaoDao.salvar` already writes the rental and its film associations in one transaction or savepoint. Its update replaces the association set. `sql/banco.sql` permits a null person address, enforces unique documents per person table and restricts deletion of referenced clients, films and addresses. Deleting a rental cascades only to its association rows.
- `build-ant.xml` compiles Jakarta servlets with Java 21 against Tomcat's `lib/servlet-api.jar` and packages `dist/projeto-jean.war`. `nbproject/project.properties` sets Java 26 for the separate NetBeans build. `Database.conectar` uses MySQL on loopback, database `projeto-jean-2026`, and reads `user` and `password` from a file selected by the JVM property `locadora.config`. Keep that file outside the repository. `web/META-INF/context.xml` and `ops/apache-projeto-jean.conf` use `/projeto-jean`.
- Planning host has JDK 21, MySQL client 8.4.9 and a listener at loopback port 3306. That listener's database contents and credentials were not inspected. `ant -f build-ant.xml war` stopped before compilation because `ant` was unavailable on PATH. No Tomcat runtime was verified.

## Agreed design

- Provide `/filmes`, `/enderecos`, `/clientes`, `/funcionarios` and `/locacoes`. `Pessoa` remains the shared base model. Addresses have a menu entry and their own CRUD. Person creation still includes inline creation of an address.
- For each resource, GET without `acao` lists with optional `q`. GET `acao=novo` displays a blank form. GET `acao=editar&id=N` preloads the edit form. GET `acao=confirmar-exclusao&id=N` displays the record label and an explicit confirmation form. POST with `acao=inserir|alterar|excluir` performs the corresponding write. Updates and deletes require positive IDs. Use a session CSRF token in every write form, including create, validate it server-side, and reject missing or invalid tokens with 403 and no write. Redirect successful writes to the same resource list with a fixed success indicator interpreted by the JSP. Return 400 for invalid input/action, 404 for missing IDs in a lookup or stale write, 409 for duplicate documents and FK conflicts, and 500 for unexpected database failures. Use an escaped JSP message and server-side logging without credential or submitted field values. Deletion never happens on GET.
- Servlets parse and validate inputs, call DAOs, set request attributes and forward with `RequestDispatcher`. JSPs under `web/WEB-INF/views` handle HTML, Portuguese captions, empty states, link construction and formatting. Use context-relative URLs, UTF-8, escaped text and attribute values, fixed internal view paths and no database access from JSPs. A small `views.Html` helper provides escaping in JSPs without adding a tag-library dependency.
- DAO lookup returns an entity or null. Search returns materialized data before JDBC resources close. Keep existing filters and descending ID order for the four existing lists. Keep SQL parameters bound. Use affected-row counts to identify a vanished record. On an update reporting zero, confirm existence on the same connection to distinguish a missing record from an unchanged save where the connector reports changed rows. Use database constraints to handle concurrent changes.
- Keep original registration dates on all edits. On a person edit, display the linked address and a link to its own edit screen. A legacy person with no address can still save other edits and can add an address through the existing address fields. An address edited directly is updated in place, so all records linked to it see that change. Person deletion retains the address for independent management. The FK blocks removal while linked. No SQL schema changes are needed.
- The runbook below uses PowerShell 7. Every command runs from the repository root unless stated otherwise. `$env:CATALINA_HOME` is a local Tomcat 10.1 installation and `$env:CATALINA_BASE` is an isolated writable Tomcat instance with HTTP port 8080 and auto-deploy enabled. Use the matching Tomcat scripts on Linux if that is the execution environment. The local MySQL database must be disposable because the script and application use a fixed name. Use the corresponding separate application config via `-Dlocadora.config` in Tomcat's JVM options. For later tasks, rebuild and copy the WAR again and wait for redeployment before checking HTTP. Run the startup script only when the test instance is stopped.

## Ordered tasks

### T01 — Verify the baseline build and local fixtures

- Objective — Establish a reproducible baseline on disposable infrastructure.
- Files — None.
- Symbols — Ant targets `compile` and `war`, `Database.conectar`, `ClassServletBase`.
- Expected behavior — Set up Ant, Java 21 and Tomcat 10.1. Verify that the selected local MySQL instance is disposable before loading `sql/banco.sql`. Avoid using the existing installation until its isolation is established.
- Prerequisites — None. Ant on PATH, Tomcat installed, an isolated MySQL 8 instance listening on loopback, and local test credentials configured outside Git.
- Constraints — Work in the checkout and the isolated runtime only. The Ant compile target clears the generated `build-ant/classes` directory. Never run data-changing validation against production or existing user data.
- Acceptance criteria — The unchanged WAR builds, deploys and serves the index. A test film created through the existing form can be found through the existing search. Record tool versions and any baseline limitation.
- Validation — In the repository root, run `ant -f build-ant.xml "-Dtomcat.home=$env:CATALINA_HOME" war` and expect `BUILD SUCCESSFUL` and `dist/projeto-jean.war`. On the disposable instance, import via `mysql -h 127.0.0.1 -u <test-user> -p` followed by `source sql/banco.sql` at the MySQL prompt. Copy the WAR with `Copy-Item dist/projeto-jean.war -Destination (Join-Path $env:CATALINA_BASE 'webapps')` and run `& "$env:CATALINA_HOME/bin/startup.bat"`. Open `http://localhost:8080/projeto-jean/index.html`, create a film from `filmes.html`, and search for that title. Expect one matching row in `ClassServletBase?tipo=filme`.

### T02 — Deliver the film CRUD and establish the JSP pattern

- Objective — Make the first complete resource work through the new request and view contract.
- Files — Modify `src/java/database/BaseDao.java`, `src/java/dao/FilmeDao.java`. Create `src/java/servlets/FilmeServlet.java`, `src/java/servlets/CrudRequest.java`, `src/java/views/Html.java`, `web/WEB-INF/views/menu.jspf`, `web/WEB-INF/views/erro.jsp`, `web/WEB-INF/views/excluir.jsp`, `web/WEB-INF/views/filmes/lista.jsp`, `web/WEB-INF/views/filmes/form.jsp`, `test/crud-smoke.ps1`.
- Symbols — `FilmeDao.findById(int)`, `FilmeDao.search(String)`, `BaseDao.update`, `BaseDao.delete`, `FilmeServlet.doGet/doPost`, `CrudRequest`, `Html.escape`. Change both base write methods to return `int`. Keep `LocacaoDao.update` compatible by adapting its override in this task too.
- Additional file — Modify `src/java/dao/LocacaoDao.java` solely to return the parent update count from the existing transactional path. If an update returns zero, verify the parent exists inside the transaction before touching associations. Roll back and return zero for a missing parent, or continue for an unchanged parent whose associations may differ.
- Expected behavior — Follow the agreed routes. Preserve all current film columns and search by title or genre. Keep current field names, limits and validation from `ClassServletBase` and `web/filmes.html`, including note 0–10 and daily price 0–9999999999.99. Reuse one JSP for create/edit. Build the shared fragments, CSRF, error forwarding and field parsing for later controllers. The temporary JSP menu can link to existing pages for resources still on the old implementation. Add a PowerShell 7 smoke script accepting `-BaseUrl` with a shared web session and assertions for the film flow. Give fixtures unique names and allow only explicit use with a disposable database.
- Prerequisites — T01.
- Constraints — Keep the legacy endpoint and its behavior until T07. The new servlet contains no HTML.
- Acceptance criteria — Create, search, prefill, change, save unchanged, confirm and delete a film. Invalid fields or CSRF do not write. A missing record produces 404. A read-only exclusion confirmation leaves the row intact.
- Validation — Build and redeploy as in T01 after each task. Run `pwsh -NoProfile -File test/crud-smoke.ps1 -BaseUrl http://localhost:8080/projeto-jean -Entity filme`, expecting exit 0 with the preceding criteria asserted. In the browser create and edit a title containing accents and an ampersand, and confirm it renders as literal text in the list and prefilled form. Check that opening a JSP under `/WEB-INF/views` directly returns 404.

### T03 — Deliver the standalone address CRUD

- Objective — Manage addresses through their own servlet while honoring existing links.
- Files — Modify `src/java/dao/EnderecoDao.java`, `web/WEB-INF/views/menu.jspf`, `test/crud-smoke.ps1`. Create `src/java/servlets/EnderecoServlet.java`, `web/WEB-INF/views/enderecos/lista.jsp`, `web/WEB-INF/views/enderecos/form.jsp`.
- Symbols — `EnderecoDao.findById(int)`, `EnderecoDao.search(String)`, `EnderecoServlet.doGet/doPost`.
- Expected behavior — List address ID, logradouro, bairro, número, IBGE and CEP. Search by ID, logradouro, bairro or CEP. Reuse the current field rules for each address field. Display IBGE and CEP as seven- and eight-character decimal strings in the list and edit form so leading zeros survive the integer storage. Add an Endereços link to the shared menu. A linked address can be edited and cannot be deleted while referenced.
- Prerequisites — T02.
- Constraints — Preserve nullable references and FK behavior. Use the common views for confirmation and errors.
- Acceptance criteria — Standalone CRUD works and round-trips leading zeros. Confirming deletion of an address linked to either type of person yields 409 and retains both records.
- Validation — Build and redeploy as in T01. Run the T02 smoke command with `-Entity endereco` for standalone CRUD, leading zeros and GET confirmation. Create a client and an employee with their addresses using the existing pages. Open each address through the new route and attempt deletion. Expect 409 and verify both people and their addresses are still listed.

### T04 — Deliver the client CRUD with integrated address creation

- Objective — Move the existing client workflow to its own servlet and JSPs.
- Files — Modify `src/java/dao/ClienteDao.java`, `src/java/dao/PessoaDao.java`, `src/java/models/Pessoa.java`, `src/java/servlets/CrudRequest.java`, `web/WEB-INF/views/menu.jspf`, `test/crud-smoke.ps1`. Create `src/java/servlets/ClienteServlet.java`, `web/WEB-INF/views/clientes/lista.jsp`, `web/WEB-INF/views/clientes/form.jsp`, `web/WEB-INF/views/pessoa-campos.jspf`.
- Symbols — `ClienteDao.findById(int)`, `ClienteDao.search(String)`, `PessoaDao.preparePessoa`, `ClienteServlet.doGet/doPost`, shared person and address field parsing.
- Expected behavior — Keep search by name/document and existing list details, including address summary and dates. Map database nulls for birth date and address. Add `Pessoa.setDt_cadastro` for DAO hydration only, keeping today's default for creation. Exclude `dt_cadastro` from UPDATE and keep the insert binding. Extract shared person and address parsing and JSP fields for T05. Preserve existing person validation. Create the person and address together using the current commit/rollback pattern. When editing an address-less client, only create an address if at least one address field was filled, then link it in the same transaction as the client update. Check the current client and address reference under a row lock for that operation. Display links to edit an existing address through T03. Treat duplicate documents and rental references on deletion as conflicts.
- Prerequisites — T02, T03.
- Constraints — Retain existing address references and creation dates during normal edits. Roll back if the client write fails after inserting a new address.
- Acceptance criteria — Existing client fields prefill, search still works, inline address creation is atomic, duplicate documents create no stray address, and linked rentals prevent client deletion. Old rows without addresses remain editable.
- Validation — Build and redeploy as in T01. Run the T02 smoke command with `-Entity cliente` to exercise CRUD and rejected duplicate creation without an orphan. On the disposable database, use a fixture with an earlier registration date and null address, edit the name without filling address fields and verify the original date and empty reference remain. Add an address in a second edit. Attempt deletion of a client referenced by a fixture rental created through the existing flow and expect 409 with both rows retained.

### T05 — Deliver the employee CRUD with integrated address creation

- Objective — Move employee operations to their own servlet and JSPs.
- Files — Modify `src/java/dao/FuncionarioDao.java`, `web/WEB-INF/views/menu.jspf`, `test/crud-smoke.ps1`. Create `src/java/servlets/FuncionarioServlet.java`, `web/WEB-INF/views/funcionarios/lista.jsp`, `web/WEB-INF/views/funcionarios/form.jsp`.
- Symbols — `FuncionarioDao.findById(int)`, `FuncionarioDao.search(String)`, `FuncionarioServlet.doGet/doPost`.
- Expected behavior — Apply T04's person/address behavior to employees. Preserve search by name/document, existing list columns, optional cargo of up to 100 characters and nonnegative integer `nivel_acesso`. Keep the original `dt_cadastro` outside UPDATE and display the persisted value when listing.
- Prerequisites — T04.
- Constraints — Reuse the T04 person field fragment and validation. Deleting an employee retains the address.
- Acceptance criteria — CRUD works, cargo/level prefill and save, invalid levels and duplicate documents leave data unchanged, and deleting an employee does not remove its address.
- Validation — Build and redeploy as in T01. Run the T02 smoke command with `-Entity funcionario`. Exercise the T04 legacy null-address fixture steps on an employee. After deleting a disposable employee, confirm the linked address remains visible and can be removed independently.

### T06 — Deliver rental CRUD without losing rental dates or links

- Objective — Manage rentals and their film set through one servlet and JSP flow.
- Files — Modify `src/java/dao/LocacaoDao.java`, `src/java/models/Locacao.java`, `web/WEB-INF/views/menu.jspf`, `test/crud-smoke.ps1`. Create `src/java/servlets/LocacaoServlet.java`, `web/WEB-INF/views/locacoes/lista.jsp`, `web/WEB-INF/views/locacoes/form.jsp`.
- Symbols — `LocacaoDao.findById(int)`, `LocacaoDao.search(String)`, `LocacaoDao.salvar`, `Locacao.getData_cadastro`, `LocacaoServlet.doGet/doPost`.
- Expected behavior — Load the original registration date and all films for each rental and prefill comma-separated film IDs. Keep list/search by client name or film title and show all films for matching rentals even when only one film matched. Include a registration-date constructor for mapping stored rentals, preserving the current no-argument behavior for creation. Validate positive client ID, film IDs and day count, distinct film IDs, at least one film and optional return date on or after the persisted rental date. Exclude `data_cadastro` from UPDATE and keep the existing transactional replacement of film links. If a new link is invalid, roll back parent changes and link changes. On confirmed deletion, rely on the existing cascade for `locacao_filme` and keep client and films. Use links to client and film lists beside the code inputs.
- Prerequisites — T02, T04 and T05.
- Constraints — Keep an open rental's return date nullable. On update, read the stored date when validating. Never accept a registration date from form data. Keep JSPs independent of JDBC.
- Acceptance criteria — Full rental CRUD works for multiple films. Historical rentals can be edited without changing registration date. Reject missing FKs, repeated film IDs and return dates earlier than registration. Deletion removes only the rental and its links.
- Validation — Build and redeploy as in T01. Run the T02 smoke command with `-Entity locacao` for successful CRUD, duplicate film validation and rollback when adding a nonexistent film during update. On the disposable database create a rental fixture with an earlier registration date. Save a valid return date between that date and today, then clear it. Search by one film title and verify the other film still appears. Before deleting the rental, try to delete a linked film and expect 409. After deletion verify no association rows remain and the client and films still exist.

### T07 — Complete navigation and remove the mixed servlet

- Objective — Route the whole application through the new controllers and JSPs.
- Files — Modify `web/index.html`, `web/WEB-INF/views/menu.jspf`, `src/java/servlets/ClienteServlet.java`, `src/java/servlets/EnderecoServlet.java`, `src/java/servlets/FilmeServlet.java`, `src/java/servlets/FuncionarioServlet.java`, `src/java/servlets/LocacaoServlet.java` and `test/crud-smoke.ps1` to migrate the test fixtures from the retired route. Remove `src/java/ClassServletBase.java`, `web/clientes.html`, `web/enderecos.html`, `web/filmes.html`, `web/funcionarios.html`, `web/locacoes.html`.
- Symbols — Home links, JSP menu, former `ClassServletBase` annotation, five new servlet mappings.
- Expected behavior — Link from the home page and every resource to the five list routes. Keep `index.html` as the welcome page. Preserve bookmarks to the old HTML pages by mapping each old URL in its corresponding servlet and redirecting GET to the list via the current context path. Reject POST on those aliases. Remove the mixed servlet and the bypassed forms once all resource paths pass. The former `/ClassServletBase?tipo=...` route is retired. No tracked consumer of it exists outside the pages being replaced.
- Prerequisites — T02, T03, T04, T05, T06.
- Constraints — Keep default HTML controls and tables, original language, no frontend framework and no changes to the WAR path or proxy configuration.
- Acceptance criteria — All five sections are reachable from home and from each other. Every form targets its own servlet. No servlet constructs HTML or performs rendering queries. Old form URLs lead to their replacement lists. Search still works after changes and refresh after a successful POST performs a GET.
- Validation — Build and redeploy as in T01, then run the smoke command for `filme`, `endereco`, `cliente`, `funcionario` and `locacao`. Visit every menu link and old HTML URL at the deployed context. Confirm a successful edit redirects and refresh does not repeat the write. Run `rg -n 'ClassServletBase|PrintWriter|getWriter\(|<form' src/java web` and expect matches only for markup in views, with no remaining legacy references. Run `git diff --check` and expect no output.

## Risks and replanning

- Client, film and address deletion is restricted while referenced. The UI explains the current link and leaves the data intact. Removing a rental removes its links by design. Address updates apply to all linked people.
- Build with the Ant WAR path verified in T01. The existing NetBeans Java 26 configuration is a separate path and remains unchanged. Only the local build and disposable deployment are required for execution acceptance.
- Apply the new WAR over a verified baseline after completing T07. No schema migration is planned. Retain the previous WAR for rollback. Rolling back the application does not reverse CRUD actions already made against a database.
- T01 needs Ant, a working Tomcat and an isolated database/config. The runtime acceptance of subsequent tasks depends on that baseline. Replan if the deployed schema differs, external clients still rely on `/ClassServletBase`, address ownership changes or an authorization requirement alters write access.
