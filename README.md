
# BUPT International College TA Recruitment System
# EBU6304 Software Engineering Group24 Repo.

## 📚 Group name list

- Norman-Ou: 190898878 (Support TA)
- DavidChen3351 : 231220600 (lead)
- 2023213105 : 231222925 (member)
- lizexi20050819-hue: 231221180 (member)
- xinpengtian923-cell：231221135（member)
- Spica1225：231220611(member)
- Lvco307：231222419(member)
- {github user name}: {qmid} (lead/member)


## Technical Stack

- Backend and frontend architecture: Java Web application based on Servlet + JSP
- Runtime environment: Servlet container such as Apache Tomcat
- Page technology: JSP for view rendering, Servlet for request handling and business logic
- Styling: plain CSS
- Data storage format: local `.json` files only
- Database usage: none

## Data Storage

The project does not use any database. All business data is stored in local JSON files, which satisfies the requirement of using `.txt`, `.csv`, `.json`, or `.xml` files.

Current data file locations:

- User data: [src/main/webapp/data/users.json](/Users/kamesan/Desktop/TA-Recruitment/src/main/webapp/data/users.json)
- Job data: [src/main/webapp/data/jobs.json](/Users/kamesan/Desktop/TA-Recruitment/src/main/webapp/data/jobs.json)
- Application data: [src/main/webapp/data/applications.json](/Users/kamesan/Desktop/TA-Recruitment/src/main/webapp/data/applications.json)
- Uploaded resumes: [src/main/webapp/uploads](/Users/kamesan/Desktop/TA-Recruitment/src/main/webapp/uploads)

## Functional Modules

Currently implemented CORE functions(V1.0):

- Login and role-based access control for TA, Teacher, and Admin
- TA account registration
- TA position list display
- TA position sorting by deadline and course name
- TA application submission
- Resume upload with file type restriction and automatic file renaming
- Submission validation for deadline and remaining quota
- Teacher position publishing
- Teacher dashboard for reviewing only their own course applications
- Teacher decision handling for accept/reject actions
- Application status color display
- Admin account management
- Admin role update, password reset, enable/disable control
- Admin teacher account creation
- Protected deletion rules for teachers with active recruitment

## Project Notes

- System interface language: English
- Target scenario: BUPT International College teaching assistant recruitment
- Data is suitable for course project demonstration and local deployment
