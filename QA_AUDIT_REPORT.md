# AI Recruitment ATS Complete QA Audit

**Audit date:** 2026-09-19  
**Scope:** Existing backend, frontend, live local application, API boundary, persistence model, security configuration, AI implementation, UI smoke tests, responsive smoke tests, and available automated tests.  
**Change policy followed:** No application source, database data, or configuration was modified. Only this report was created.

## A. Executive Summary

The application builds and starts, and the seven existing backend unit tests pass. The live database contains seeded users, jobs, and at least one application. Public job browsing and normal admin read-only navigation work.

This is not release-ready for a frontend redesign or production use. The most important blockers are committed/default credentials and JWT/database secrets, a public `/api/auth/me` route that produces HTTP 500, browser `localStorage` token storage, a mobile layout with no navigation control, an application-status contract mismatch that breaks the OFFER stage, incomplete resume lifecycle, weak test coverage, and role/ownership inconsistencies for hiring managers. No external AI provider is integrated; the current "AI" feature is deterministic keyword scoring.

The audit could not safely execute create/update/delete workflows, duplicate registration, applications, job status changes, uploads, or user deletion because the request explicitly prohibited changing database data. Those flows are assessed statically and are marked as requiring a disposable test database for final verification.

## Verified Build and Runtime Evidence

- Backend: `mvn test` passed, 7 tests, 0 failures, 0 errors.
- Frontend: `npm run build` passed; Vite transformed 112 modules and produced a 337.69 kB JavaScript bundle before gzip.
- Live `POST /api/auth/login` with the documented demo account returned a valid ADMIN token.
- Live unauthenticated `GET /api/auth/me` returned HTTP 500 with `Internal server error`.
- Live unauthenticated `GET /api/jobs` returned HTTP 200 and exposed recruiter name and email.
- Live unauthenticated admin and candidate endpoints returned HTTP 401.
- Login and registration had no horizontal overflow at 320, 375, 390, 430, 768, 1024, 1280, 1366, 1440, and 1920 px.
- Desktop admin read-only routes loaded without failed requests during smoke navigation.
- At 320 px the authenticated sidebar disappears and no menu/toggle is present; there is no usable in-app navigation.

## B. Critical Issues

### QA-SEC-001: Default credentials and secrets are committed

- **Module:** Security, configuration, deployment
- **Severity:** Critical
- **Exact problem:** The repository contains a fallback PostgreSQL password, a development JWT secret, a seeded administrator password, and frontend-visible demo credentials.
- **Steps to reproduce:** Inspect `application.properties`, `DataInitializer.java`, `Login.jsx`, and the README; start the application without overriding all environment variables; use the documented admin account.
- **Expected:** Secrets are injected through deployment secret storage; no default privileged account is usable outside an explicitly isolated development profile.
- **Actual:** Defaults are present in source and the documented admin login works against the live local instance.
- **Root cause:** Hard-coded fallback values and unconditional admin seeding.
- **Relevant files:** [application.properties](recruitment-ats-backend/src/main/resources/application.properties), [DataInitializer.java](recruitment-ats-backend/src/main/java/com/ats/config/DataInitializer.java), [Login.jsx](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/pages/Login.jsx).
- **Recommended fix:** Remove committed secrets and demo credentials, require environment-provided secrets at startup, and gate development seeding behind an explicit non-production profile with generated/rotated credentials.

### QA-SEC-002: JWT bearer tokens are stored in localStorage

- **Module:** Authentication/session security
- **Severity:** Critical
- **Exact problem:** The access token is persisted in `localStorage`, making it directly readable by any successful XSS payload or compromised third-party script.
- **Steps to reproduce:** Sign in and inspect browser storage; observe `token` under local storage.
- **Expected:** Session credentials are protected from JavaScript access where practical, with secure, HttpOnly, SameSite cookies and appropriate CSRF protection, or a documented risk-accepted architecture.
- **Actual:** The bearer token is readable by all page JavaScript and attached to every Axios request.
- **Root cause:** `saveAuth` and the request interceptor use localStorage.
- **Relevant files:** [api.js](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/api.js).
- **Recommended fix:** Adopt a secure cookie/session design or formally threat-model and harden the token architecture, including CSP and XSS controls.

## C. High Issues

### QA-API-001: Public `/api/auth/me` produces HTTP 500

- **Module:** Authentication API, error handling
- **Severity:** High
- **Steps to reproduce:** Send `GET http://localhost:8080/api/auth/me` without an Authorization header.
- **Expected:** HTTP 401 with the standard authentication-required response.
- **Actual:** HTTP 500 `{"success":false,"message":"Internal server error","data":null}`.
- **Root cause:** `/api/auth/**` is fully permitted, while the controller assumes a non-null authenticated principal.
- **Relevant files:** [SecurityConfig.java](recruitment-ats-backend/src/main/java/com/ats/security/SecurityConfig.java), [AuthController.java](recruitment-ats-backend/src/main/java/com/ats/auth/AuthController.java).
- **Recommended fix:** Require authentication for `/auth/me` and/or explicitly handle anonymous authentication; add controller/security integration tests.

### QA-FUNC-001: Frontend OFFERED status does not match backend OFFER

- **Module:** Applications/pipeline
- **Severity:** High
- **Steps to reproduce:** As recruiter/admin, move an application to the frontend's `OFFERED` stage.
- **Expected:** The application reaches the offer stage successfully.
- **Actual:** The frontend sends `OFFERED`; the backend enum accepts `OFFER`, so enum binding returns a 400.
- **Root cause:** Duplicated, inconsistent status contracts.
- **Relevant files:** [Applications.jsx](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/pages/Applications.jsx), [ApplicationStatus.java](recruitment-ats-backend/src/main/java/com/ats/application/ApplicationStatus.java), [ApplicationService.java](recruitment-ats-backend/src/main/java/com/ats/application/ApplicationService.java).
- **Recommended fix:** Define one shared API contract and test every transition end to end.

### QA-AUTHZ-001: Hiring-manager application access is ownership-inconsistent

- **Module:** RBAC/application management
- **Severity:** High
- **Steps to reproduce:** Authenticate as a hiring manager who owns or belongs to the company for a job created by a recruiter; request that job's applications or update an application status.
- **Expected:** Access follows the documented hiring-manager/company permission model.
- **Actual:** Access is granted only when the current user is the job's recruiter, so the UI-exposed manager workflow is denied for recruiter-owned jobs.
- **Root cause:** `verifyStaffAccess` checks recruiter identity but not company ownership/manager relationship.
- **Relevant file:** [ApplicationService.java](recruitment-ats-backend/src/main/java/com/ats/application/ApplicationService.java).
- **Recommended fix:** Define and enforce one ownership policy across jobs, applications, interviews, and AI results; add cross-role integration tests.

### QA-AUTHZ-002: AI matching repeats the hiring-manager ownership defect

- **Module:** AI matching authorization
- **Severity:** High
- **Steps to reproduce:** As a hiring manager, request or calculate an AI match for an application on a recruiter-owned job in the same company.
- **Expected:** Behavior matches the hiring-manager permission model.
- **Actual:** Access is denied unless the manager is also the job recruiter.
- **Root cause:** `verifyAccess` uses recruiter identity rather than the authorized company/staff relationship.
- **Relevant file:** [AiMatchingService.java](recruitment-ats-backend/src/main/java/com/ats/ai/AiMatchingService.java).
- **Recommended fix:** Centralize authorization policy and test candidate, recruiter, manager, admin, and unrelated-user access for every object.

### QA-UI-001: Mobile authenticated navigation is unavailable

- **Module:** Responsive UI/navigation
- **Severity:** High
- **Steps to reproduce:** Sign in, set viewport to 320 px, and open `/admin/dashboard` or an equivalent role dashboard.
- **Expected:** A usable mobile navigation control exposes all permitted sections.
- **Actual:** The sidebar is hidden; the page shows only a brand/header and content. No hamburger/menu/toggle is available.
- **Root cause:** Mobile CSS hides the fixed sidebar while `Layout.jsx` has no mobile navigation control.
- **Relevant files:** [Layout.jsx](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/components/Layout.jsx), [styles.css](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/styles.css).
- **Recommended fix:** Add an accessible menu control, focus management, close behavior, and keyboard support; test all roles at mobile widths.

## D. Medium Issues

### QA-SEC-003: Public job responses expose recruiter identity/email

- **Module:** Data exposure/API
- **Severity:** Medium
- **Steps to reproduce:** Send unauthenticated `GET /api/jobs`.
- **Expected:** Public job data contains only intentionally public company/job fields.
- **Actual:** Each job response includes recruiter name and email; live response confirmed `rajesh@gmail.com`.
- **Root cause:** Public endpoint serializes the full recruiter summary in `JobResponse`.
- **Relevant files:** [JobController.java](recruitment-ats-backend/src/main/java/com/ats/job/JobController.java), [JobResponse.java](recruitment-ats-backend/src/main/java/com/ats/job/JobResponse.java).
- **Recommended fix:** Use a public response DTO without personal recruiter contact data.

### QA-DATA-001: Admin user deletion has no complete dependent-record policy

- **Module:** Database integrity/admin
- **Severity:** Medium
- **Steps to reproduce:** In a disposable database, delete a user with related profile, company, job, application, interview, notification, or AI records.
- **Expected:** Explicit cascade/soft-delete behavior preserves referential integrity and auditability.
- **Actual:** `AdminService.delete` calls `userRepository.deleteById`; entity relationships do not establish a complete, verified cleanup policy. Runtime deletion was not executed because data mutation was prohibited.
- **Root cause:** Deletion behavior is not modeled as an explicit domain operation.
- **Relevant files:** [AdminService.java](recruitment-ats-backend/src/main/java/com/ats/admin/AdminService.java), user/domain entity classes.
- **Recommended fix:** Prefer soft deletion for identity records or define tested cascades and cleanup for every dependent entity.

### QA-FUNC-002: Resume management is upload-only

- **Module:** Resume/file workflow
- **Severity:** Medium
- **Exact problem:** There are no API or UI paths to list, view/download, replace, or delete a resume.
- **Steps to reproduce:** Inspect `/api/resumes` mappings and the candidate Resume page.
- **Expected:** Candidate can manage the complete supported resume lifecycle and authorized staff can view it where required.
- **Actual:** Only upload/parse is implemented; the UI can display the upload response but cannot retrieve an existing stored file after refresh.
- **Root cause:** Missing endpoints/service methods and no persistent retrieval workflow.
- **Relevant files:** [ResumeController.java](recruitment-ats-backend/src/main/java/com/ats/resume/ResumeController.java), [ResumeService.java](recruitment-ats-backend/src/main/java/com/ats/resume/ResumeService.java), [Resume.jsx](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/pages/Resume.jsx).
- **Recommended fix:** Add authenticated, ownership-checked metadata/download/delete/replace operations with content-disposition and audit controls.

### QA-FUNC-003: Upload failure can orphan a file

- **Module:** File storage/database consistency
- **Severity:** Medium
- **Steps to reproduce:** Submit a valid file that is copied successfully, then cause extraction or persistence to fail in a disposable environment; inspect `uploads/resumes`.
- **Expected:** File and database record commit atomically or failed files are removed.
- **Actual:** The file is copied before extraction and repository save; later failure can leave an untracked file.
- **Root cause:** File-system operation is not coordinated with transaction/error cleanup.
- **Relevant file:** [ResumeService.java](recruitment-ats-backend/src/main/java/com/ats/resume/ResumeService.java).
- **Recommended fix:** Use temporary files, transactional cleanup, and a reconciliation job.

### QA-FUNC-004: Notifications have no in-repository producers

- **Module:** Notifications
- **Severity:** Medium
- **Steps to reproduce:** Trace all writes to the notifications repository and perform status/job/application/interview changes in a disposable environment.
- **Expected:** Relevant workflow changes create notifications for affected users.
- **Actual:** The API reads notifications and marks them read, but no in-repository workflow producer was found; the live admin inbox was empty.
- **Root cause:** Notification creation is not integrated into domain services.
- **Relevant files:** [NotificationController.java](recruitment-ats-backend/src/main/java/com/ats/notification/NotificationController.java), notification repository/entity, application/job/interview services.
- **Recommended fix:** Add domain event/listener producers and integration tests for each notification trigger.

### QA-UI-002: Admin report response is discarded

- **Module:** Admin dashboard
- **Severity:** Medium
- **Steps to reproduce:** Open admin reports with a valid admin session.
- **Expected:** Report metrics are rendered.
- **Actual:** `Admin.jsx` accepts only arrays for `reports`; the backend report endpoint returns an object, so report data becomes an empty array and is not displayed.
- **Root cause:** Frontend response-shape assumption does not match `AdminReportResponse`.
- **Relevant files:** [Admin.jsx](recruitment-ats-frontend-part2/recruitment-ats-frontend/src/pages/Admin.jsx), [AdminController.java](recruitment-ats-backend/src/main/java/com/ats/admin/AdminController.java), [AdminReportResponse.java](recruitment-ats-backend/src/main/java/com/ats/admin/AdminReportResponse.java).
- **Recommended fix:** Model the report object explicitly and add a fixture/API contract test.

### QA-API-002: No pagination/filter/sort contract is exposed for major collections

- **Module:** API/performance/UX
- **Severity:** Medium
- **Steps to reproduce:** Inspect collection controller mappings and repository methods; request admin/users, jobs, applications, and notifications with common `page`, `size`, `sort`, and search parameters.
- **Expected:** Documented bounded pagination and consistent filtering/sorting.
- **Actual:** Responses are list-shaped and controller/repository inventory shows no general pagination contract. Frontend pages load whole collections.
- **Root cause:** Unbounded list APIs and client-side-only display assumptions.
- **Recommended fix:** Add validated server-side pagination and indexed filters before production-scale use.

### QA-ERR-001: Generic exception handling masks diagnosable API failures

- **Module:** Error handling/observability
- **Severity:** Medium
- **Steps to reproduce:** Trigger an unexpected controller/service failure, including unauthenticated `/api/auth/me`.
- **Expected:** Stable safe error envelope with correct status and correlation ID, while details remain in server logs.
- **Actual:** Broad `Exception` handling converts unrelated failures to generic 500; clients cannot distinguish missing data, database failure, or programming error.
- **Root cause:** Catch-all handler without typed persistence/validation/service error taxonomy.
- **Relevant file:** [GlobalExceptionHandler.java](recruitment-ats-backend/src/main/java/com/ats/common/GlobalExceptionHandler.java).
- **Recommended fix:** Add typed handlers, correlation IDs, structured logging, and contract tests for 400/401/403/404/409/413/429/500.

## E. Low Issues

### QA-TEST-001: Automated coverage is insufficient for release confidence

- **Module:** Test strategy
- **Severity:** Low (process risk, high practical impact)
- **Exact problem:** Only seven Mockito service tests exist: three company tests and four job tests. There are no controller, security, JWT, repository, upload, AI, application, interview, notification, integration, or frontend tests.
- **Expected:** Critical user journeys and authorization boundaries are executable in CI.
- **Actual:** Build green status covers a small fraction of the application.
- **Relevant files:** `recruitment-ats-backend/src/test/java/com/ats/company/CompanyServiceTest.java`, `recruitment-ats-backend/src/test/java/com/ats/job/JobServiceTest.java`.
- **Recommended fix:** Add Testcontainers/integration coverage, API contract tests, frontend component/e2e coverage, and a disposable seeded test dataset.

### QA-AI-001: "AI" is deterministic keyword scoring, not an external AI integration

- **Module:** AI/product behavior
- **Severity:** Low
- **Exact problem:** No external model, provider, timeout, retry, API key, or AI network integration exists. Matching uses weighted text/skill rules.
- **Expected:** Product claims and QA scope reflect the actual decision-support algorithm, or provider behavior is implemented and tested.
- **Actual:** Matching is deterministic and can produce misleading scores for synonyms, poor-quality input, and unstructured resumes.
- **Relevant file:** [AiMatchingService.java](recruitment-ats-backend/src/main/java/com/ats/ai/AiMatchingService.java).
- **Recommended fix:** Document the algorithm and limitations, add score fixtures/edge-case tests, and avoid presenting it as model-backed AI unless that changes.

### QA-FUNC-005: DOC/DOCX parsing support is not proven by the dependency set

- **Module:** Resume parsing
- **Severity:** Low
- **Steps to reproduce:** Upload representative DOC and DOCX files in a disposable environment and compare extracted text/skills.
- **Expected:** Supported formats parse consistently or return a precise unsupported-format error.
- **Actual:** Runtime behavior is unverified; the project has `tika-core` but no dedicated parser bundle visible in the dependency list.
- **Relevant files:** [pom.xml](recruitment-ats-backend/pom.xml), [ResumeService.java](recruitment-ats-backend/src/main/java/com/ats/resume/ResumeService.java).
- **Recommended fix:** Add format fixtures and verify parser dependencies and extraction output.

## F. Security Findings

- **Authentication:** BCrypt is used; JWTs contain email, user ID, name, role, issue and expiry claims. There is no refresh-token or explicit revocation/logout mechanism; logout is client-side token removal.
- **Authorization:** URL-level role rules exist for admin/candidate/recruiter/manager prefixes, while many business endpoints rely on manual service checks. This creates inconsistent policy risk.
- **IDOR/BOLA:** Candidate application/profile/resume services include ownership checks. Admin/staff object-level access requires broader cross-role tests, especially applications, AI results, interviews, and files.
- **CSRF:** CSRF is disabled because the app is stateless bearer-token based. This is consistent only while tokens remain outside cookies; any cookie migration requires CSRF design.
- **CORS:** Origins are allowlisted to localhost ports 3000, 4200, and 5173; production origin configuration and credentials behavior were not verified.
- **File uploads:** Extension and Tika MIME checks exist, size is capped at 10 MB, stored names are UUID-prefixed/sanitized, and files are stored outside the classpath. No download authorization, malware scanning, quota, cleanup, or atomic lifecycle was found.
- **XSS/injection:** React rendering is generally escaped; no `dangerouslySetInnerHTML` was found in the frontend inventory. Input validation and payload-size/rate-limit behavior need negative integration tests.
- **Rate limiting:** No application-level rate limiting was found for login, registration, upload, AI matching, or public jobs.
- **Secrets/debug:** Default secrets and demo credentials are the largest exposure. No separate production profile/secret enforcement was verified.

## G. API Findings and Endpoint Inventory

### Endpoint inventory

- Auth: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.
- Admin: user list/detail/update/delete, jobs, applications, reports, notifications under `/api/admin/**`.
- Jobs: published list, detail, create, status update, recruiter jobs under `/api/jobs/**`.
- Companies: create, own companies, companies by owner under `/api/companies/**`.
- Applications: apply, candidate applications, job applications, status update under `/api/applications/**`.
- Candidate profiles: save and retrieve under `/api/candidate-profiles/**`.
- Resumes: upload under `/api/resumes/**`.
- Interviews: create, status update, list, and `/my` compatibility endpoint under `/api/interviews/**`.
- Notifications: current user, unread, user-specific, mark-one-read, mark-all-read under `/api/notifications/**`.
- AI: calculate and retrieve match results under `/api/ai/**`.

### API test coverage status

- Valid GET smoke checks: public jobs, admin dashboard collections, and admin UI route loading passed with current data.
- Unauthorized GET checks: admin and candidate protected endpoints returned 401.
- Unauthenticated auth/me: failed with 500, QA-API-001.
- Valid POST login: passed with the documented admin account.
- Mutating valid/duplicate/invalid payload tests: not executed to preserve database state.
- 404/405/413 envelopes are implemented in code but need live contract tests.
- 409 and 429 behavior was not found as a dedicated, verified API contract.
- Database failure, malformed response, network failure, retry, and timeout behavior remain unverified end to end.

## H. Database Findings

- PostgreSQL with `spring.jpa.hibernate.ddl-auto=update` is configured. This is risky for production schema governance and migration repeatability.
- Entity relationships include users, companies, jobs, candidate profiles, resumes, applications, interviews, notifications, and AI results.
- A uniqueness constraint exists for `(job_id, candidate_id)`, and the service also checks duplicates before save. Concurrent duplicate requests still require a database-level integration test.
- Candidate profile has a unique user relationship.
- User deletion behavior is not a complete explicit cascade/soft-delete design; see QA-DATA-001.
- Orphan files are possible even if database rows remain consistent; see QA-FUNC-003.
- Actual orphan/null/reference consistency beyond currently returned data was not queried because database changes and direct data manipulation were prohibited.

## I. AI Findings

- Implemented feature: weighted matching across skills (40%), experience (25%), education (10%), certifications (5%), role (10%), and text similarity (10%).
- There is no resume parsing model, recommendation model, LLM, external provider, retry policy, timeout, or invalid-model-response handler.
- Null/blank fields are handled with fixed scoring rules, but semantic quality, synonym handling, long input, poor-quality resumes, and scoring fairness are not covered by tests.
- Candidate/recruiter/admin authorization exists in the AI service; hiring-manager authorization is inconsistent with the rest of the intended workflow.
- AI results are persisted one-to-one per application; concurrent calculation and stale-result behavior need integration testing.

## J. UI/UX Findings

- Login and registration are visually coherent in the tested states and have visible labels.
- Desktop admin routes loaded with current seeded data and no observed failed requests.
- Mobile authenticated navigation is unavailable at 320 px; see QA-UI-001.
- Admin report data is not rendered due to response-shape mismatch; see QA-UI-002.
- The login screen displays privileged demo credentials in the UI; see QA-SEC-001.
- Loading/error/empty states exist in several pages, but complete failure-state coverage was not possible without deliberately disrupting services.
- Accessibility review is incomplete: native labels are present on login/register; modal focus trapping, focus restoration, keyboard-only workflows, contrast, and screen-reader announcements require dedicated testing.
- UI uses text glyphs for icons rather than a consistent icon system; this may create cross-platform rendering and accessibility inconsistencies.

## K. Responsive Findings

- Login and registration: no horizontal overflow observed at 320, 375, 390, 430, 768, 1024, 1280, 1366, 1440, and 1920 px.
- Admin dashboard: no horizontal overflow at 320 px, but sidebar/navigation is hidden with no replacement control.
- Jobs, applications, candidate, recruiter, manager, modal, and table views require per-role/per-route viewport testing on a disposable authenticated session; they were not all exercised because only the demo admin account was safely available for read-only checks.

## L. Performance Findings

- Frontend production bundle is 337.69 kB raw JS / 106.46 kB gzip; acceptable for a small SPA but should be monitored before adding redesign assets.
- Admin page issues four collection requests in parallel and loads whole collections; this will scale poorly without server-side pagination.
- No rate limiting was found for expensive upload/AI endpoints.
- PDF/DOC parsing is synchronous inside the request flow; large valid files can block request threads up to the 10 MB limit.
- No query profiling, index review, load test, or memory/leak test was performed.

## M. Regression Risks Before Redesign

- Changing route/layout structure can break role redirects and direct URL access because route permissions and navigation definitions are duplicated across `App.jsx` and `Layout.jsx`.
- Changing API DTOs can break report rendering and the public job response contract.
- Any change to application status labels can break transition validation and persisted enum values.
- Resume UI redesign may imply download/replace/delete behavior that does not exist in the backend.
- Mobile redesign must restore navigation, not only adjust dimensions.
- Auth redesign must preserve session-expiration handling and avoid worsening localStorage token exposure.

## N. Complete Test-Case Checklist

Legend: **PASS** = executed and passed; **FAIL** = executed and failed; **CODE RISK** = identified statically; **BLOCKED** = intentionally not executed because it mutates data or needs a disposable environment; **NOT VERIFIED** = requires additional environment/tooling.

| Area | Test case | Result |
|---|---|---|
| Build | Backend compile/test | PASS: 7/7 |
| Build | Frontend production build | PASS |
| Architecture | Frontend/backend/database/API inventory | PASS: inventoried |
| Authentication | Valid login | PASS with seeded demo admin |
| Authentication | Invalid login | BLOCKED for exhaustive matrix |
| Authentication | Registration/duplicate email | BLOCKED: mutates database |
| Authentication | Invalid/weak/empty fields | BLOCKED for complete API matrix |
| Authentication | Logout | CODE RISK: client-side token removal only |
| Authentication | Session persistence/refresh | NOT VERIFIED end to end |
| Authentication | Session expiration | CODE REVIEWED, live expiry not waited/tested |
| Authentication | Protected direct URL access | CODE REVIEWED; unauthenticated API boundary partly tested |
| Authorization | Admin protected API | PASS: unauthenticated request returned 401 |
| Authorization | Candidate/recruiter/manager cross-role matrix | BLOCKED: requires test accounts |
| Candidate | Register to dashboard | BLOCKED: registration mutates database |
| Candidate | Profile save/update | BLOCKED |
| Candidate | Resume PDF/DOC/DOCX/invalid/corrupt/large/empty | BLOCKED: upload mutates file/database |
| Candidate | Job search/details | PASS for public jobs GET; filters/pagination not verified |
| Candidate | Apply/duplicate/closed/missing profile | BLOCKED |
| Candidate | Application status/history | BLOCKED; status mismatch found statically |
| Recruiter | Create/edit/publish/close job | BLOCKED |
| Recruiter | Applicants/search/filter/candidate profile | BLOCKED |
| Recruiter | Status transitions/shortlist/reject | BLOCKED; transition code reviewed |
| Recruiter | Interview scheduling/notifications | BLOCKED; notification producer gap found |
| Admin | Dashboard/users/jobs/applications/interviews/reports/notifications | PASS read-only smoke; report display defect found |
| Jobs | Validation/duplicates/invalid values | BLOCKED |
| Jobs | Search/filter/sort/pagination | NOT VERIFIED; no general server pagination found |
| Applications | Unauthorized/invalid ID | PARTIAL: protected boundary only |
| Applications | Duplicate/closed/deleted job/invalid transition | BLOCKED |
| Resume | Viewing/downloading/replacing/deleting | FAIL by implementation gap: endpoints absent |
| AI | Matching calculation/get | NOT VERIFIED with mutation; algorithm code reviewed |
| AI | Empty/long/poor input/provider failure/retry | NOT APPLICABLE for external provider; edge cases untested |
| API | Valid/malformed/invalid fields | PARTIAL; only safe GET/login checks |
| API | 400/401/403/404/405/413/429/500 | PARTIAL; 401 and 500 verified, others code-only/unverified |
| Security | Secrets/default credentials | FAIL: QA-SEC-001 |
| Security | Token storage | FAIL: QA-SEC-002 |
| Security | CORS | CODE REVIEWED; deployed origin not verified |
| Security | XSS/injection/CSRF | NOT VERIFIED by dynamic security tooling |
| Database | Relationships/constraints | CODE REVIEWED; live mutation/query audit blocked |
| Database | Orphans/nulls/delete/update behavior | BLOCKED |
| UI | Login/register layout/forms | PASS smoke |
| UI | Admin desktop screens | PASS read-only smoke, report issue found |
| UI | Loading/empty/error/success states | PARTIAL |
| Responsive | Requested widths | PASS login/register overflow check; authenticated nav FAIL at 320 |
| Accessibility | Labels/basic semantics | PARTIAL: login/register labels pass |
| Accessibility | Keyboard/modal/focus/contrast/ARIA | NOT VERIFIED completely |
| Performance | Bundle build | PASS build; size recorded |
| Performance | API/query/load/memory | NOT VERIFIED |
| Runtime | Console errors/failed requests | PASS for tested admin navigation; login interaction inconclusive |

## TOP ISSUES TO FIX BEFORE FRONTEND REDESIGN

1. Remove committed/default database, JWT, and admin credentials; eliminate demo credentials from the shipped UI.
2. Fix `/api/auth/me` so anonymous requests return 401, not 500, and add security integration tests.
3. Resolve the authentication storage threat model, especially the localStorage bearer token exposure.
4. Align application status values (`OFFER` versus `OFFERED`) across API, frontend, database, and tests.
5. Define and test hiring-manager/company authorization consistently across jobs, applications, interviews, and AI matches.
6. Restore mobile authenticated navigation with an accessible menu and keyboard/focus behavior.
7. Implement a complete, authorized resume lifecycle or explicitly remove unsupported UI claims: retrieve/view, replace, delete, and cleanup.
8. Wire domain actions to notification creation and verify user-targeted authorization.
9. Fix admin report response handling and add API contract tests.
10. Establish a disposable integration-test environment with role fixtures and cover the candidate, recruiter, admin, upload, AI, authorization, and failure workflows before redesign regression work.
