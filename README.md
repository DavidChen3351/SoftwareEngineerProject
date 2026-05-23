# BUPT International College TA Recruitment System

EBU6304 Software Engineering Group 24 repository.

This project is a Java Web prototype for managing BUPT International College teaching assistant recruitment. It supports TA registration and applications, teacher / module organiser job management, and administrator account control.

## Group Members

- Norman-Ou: 190898878 (Support TA)
- DavidChen3351: 231220600 (lead)
- 2023213105: 231222925 (member)
- lizexi20050819-hue: 231221180 (member)
- xinpengtian923-cell: 231221135 (member)
- Spica1225: 231220611 (member)
- Lvco307: 231222419 (member)
- {github user name}: {qmid} (lead/member)

## Technical Stack

- Java 11
- Jakarta Servlet 6.0
- JSP 3.1
- Maven WAR project
- Apache Tomcat or another Jakarta Servlet compatible container
- Plain CSS for styling
- Local JSON file persistence
- No database

## Project Structure

```text
TA-Recruitment-System/
  pom.xml
  src/main/java/com.bupt.ta/
    listener/
    model/
    servlet/
    util/
  src/main/webapp/
    admin/
    assets/
    data/
    ta/
    teacher/
    uploads/
    WEB-INF/
    index.jsp
    login.jsp
    register.jsp
```

## Data Storage

The system does not use a database. Seed data is bundled in:

- `TA-Recruitment-System/src/main/webapp/data/users.json`
- `TA-Recruitment-System/src/main/webapp/data/jobs.json`
- `TA-Recruitment-System/src/main/webapp/data/applications.json`

At runtime, `DataStore` persists data outside the deployed WAR so redeploying Tomcat does not reset users, jobs, or applications.

Default runtime data directory:

```text
<user-home>/.ta-recruitment/data
```

You can override it with the environment variable:

```text
TA_DATA_DIR
```

Uploaded CV files are stored under the web app `uploads/` directory and referenced from application records.

## Roles

The system has three roles:

- `TA`: browse positions, search/sort jobs, apply for positions, upload CV, view application results.
- `TEACHER`: publish and manage positions, review applications for their own positions, accept/reject applicants.
- `ADMIN`: manage user accounts, create teacher accounts, change roles, enable/disable users, reset passwords.

## Implemented Functional Modules

### Authentication and Account Rules

- Role-based login for TA, Teacher and Admin.
- Login by account ID / student ID, with selected role validation.
- Disabled accounts cannot log in.
- Session-based authentication using `currentUser`.
- Logout support.
- TA self-registration.
- TA registration requires:
  - BUPT email domain: `@bupt.edu.cn` or `@mail.bupt.edu.cn`
  - unique email
  - unique student ID
  - confirmed password
  - strong password: at least 8 characters, uppercase, lowercase and number

### TA Functions

- Browse available TA positions.
- Search positions by title, module code, module name, teacher, or workload.
- Sort positions by deadline or module.
- View module code, module name, workload, deadline and remaining vacancy.
- View position status:
  - `Available`
  - `Closed`
  - `No Vacancy`
- Apply only when the position is not cancelled, not paused, before deadline and has remaining quota.
- Submit application information:
  - skills
  - weekly availability
  - relevant experience
  - CV / resume
- Resume upload validation:
  - only PDF or DOCX
  - maximum 5 MB
  - automatic filename format: `StudentID_timestamp.ext`
- Duplicate application prevention for the same TA and position.
- "My applications" section showing submitted applications and result status:
  - `PENDING`
  - `ACCEPTED`
  - `REJECTED`
- TA can see reviewed time for accepted applications.
- Cancelled positions are hidden from the main TA job list and shown as cancelled in existing application history.

### Teacher / MO Functions

- Teacher dashboard only shows the logged-in teacher's own positions and applications.
- Publish new TA positions with:
  - position title
  - module code
  - module name
  - workload
  - quota
  - deadline
- Search teacher-owned positions.
- Review applications submitted to teacher-owned positions.
- Accept or reject applications.
- Accepting an application updates the filled slot count.
- Rejecting a previously accepted application releases the slot.
- Prevent accepting when:
  - the position is cancelled
  - the position is paused for new applications
  - no remaining slots exist
- Pause / resume new applications for a position.
- Update total quota, while preventing quota lower than already filled slots.
- Cancel a position and remove it from the TA portal.
- Delete a position and remove its related application records.
- Teacher-side position status explains whether a position is open, paused, full, closed, or cancelled.

### Admin Functions

- Admin dashboard with user search by name, ID, student ID or email.
- Role filter for TA / Teacher / Admin.
- Create teacher accounts.
- Edit user role.
- Enable or disable accounts.
- Batch enable / batch disable selected accounts.
- Reset password with the same strong password rule.
- Delete user accounts.
- Prevent unsafe admin actions:
  - admin cannot delete their own account
  - admin cannot disable their own account
  - admin cannot demote their own account from Admin
  - teacher with active recruitment cannot be deleted, disabled, or reassigned
- Removing TA users also removes their related applications and recalculates filled slots.

### Data Integrity

- `DataIntegrityListener` runs on application startup.
- It logs the persistent data directory.
- It recalculates each job's `filledSlots` from accepted application records.
- Legacy IntelliJ Tomcat data can be migrated into the persistent runtime directory.

## Business Rules

- A TA can apply only if:
  - the job is not cancelled
  - the teacher has not paused new applications
  - the deadline has not passed
  - remaining slots are greater than zero
  - the TA has not already applied for the same position
- A teacher can manage only their own jobs and applications.
- Admin users can manage accounts but are blocked from actions that would remove their own access or disrupt active recruitment.

## Build and Run

From the project folder:

```bash
cd TA-Recruitment-System
mvn clean package
```

The WAR file is generated at:

```text
TA-Recruitment-System/target/ta-recruitment.war
```

Deploy the WAR to Tomcat, then open the application in a browser.

For a quick compile check:

```bash
mvn compile
```

## Test Programs

The project currently includes a lightweight Java test program instead of a JUnit test suite:

```text
TA-Recruitment-System/src/test/java/com/bupt/ta/CoreRulesTest.java
```

It verifies core rules including:

- password strength validation
- SHA-256 password hashing
- job availability status: Available, Closed, No Vacancy
- paused and cancelled job application blocking
- `Job` map serialisation / deserialisation
- `ApplicationRecord.reviewedAt` compatibility

Run it from `TA-Recruitment-System/`:

```powershell
mvn -q test-compile
java -cp "target/test-classes;target/classes" com.bupt.ta.CoreRulesTest
```

Expected successful output:

```text
All core rule tests passed.
```

## Git Notes

- Root `.gitignore` ignores IDE files and generated WAR artifacts.
- Project `.gitignore` ignores Maven build output and project-local IDE files.
- Recommended workflow:
  - create a feature branch
  - commit focused changes
  - open a pull request
  - merge into `main` after review

## Project Notes

- System interface language: English
- Target scenario: BUPT International College TA recruitment
- Data is suitable for course project demonstration and local deployment
- The current implementation uses file-based storage for assignment compatibility and simplicity
