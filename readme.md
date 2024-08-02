## Table of contents
- [Introduction](#introduction)
- [Technologies](#technologies)
- [Usage](#usage)
  - [Util Controllers](#util-controllers)
    - [Authentication Controller](#authentication-controller)
    - [User Controller](#user-controller)
    - [OAuth2 Controller](#oauth2-controller)
    - [Dropbox Controller](#dropbox-controller)
    - [Google Controller](#google-controller)
    - [Swagger UI](#swagger-ui)
  - [Main Controllers](#main-controllers)
    - [Project Controller](#project-controller)
    - [Task Controller](#task-controller)
    - [Label Controller](#label-controller)
    - [Message Controller](#message-controller)
    - [Attachment Controller](#attachment-controller)
- [Configuration](#configuration)
  - [application.properties](#applicationproperties)
  - [liquibase.properties](#liquibaseproperties)
  - [11-insert-owner.yaml](#11-insert-owneryaml)
  - [.env](#env)
- [Installation](#installation)
- [Issue](#issue)

# Introduction
**The goal** of this project was to create a **java server** for an application that can be used by developers working together on **large projects** which require **complex administration**. Be aware that current state of the project **does not** include any UI and can only be properly used with additional tools like **[Postman](https://www.postman.com/)**. Here are some of the project's features:
 - Authentication using **JWT**
 - Application wide **role system**, allowing admins to manage the app
 - A system for browsing and managing projects with features such as:
    - **public** and **private** projects — public projects can be seen by logged in users
    - **comments and replies system** for tasks in projects
    - tasks can be **assigned** to project members
    - both tasks and projects have **date bounds** that infuence their status
    - many different **sorting methods** for tasks, projects, etc.
 - User management for admins
 - Projects can be connected to *Dropbox* in order to enable **file sharing** within project
 - Projects can also be connected to *Google Calendar* in order to better **track progress** and get **notifications**
 - *Third party services* are connected with **OAuth2** protocol
 - *Swagger UI* for easier integration into other projects and trying out the server

# Technologies
The project is built using mainly **Spring Framework**. Here is a complete list of technologies employed:
 - Spring Boot
 - Spring Security
 - Spring Boot Web
 - Spring Data JPA
 - JWT 
 - Lombok
 - Mapstruct
 - Swagger
 - MySQL 
 - Liquibase
 - Dropbox Java SDK
 - Google Calendar Java SDK
 - Custom OAuth2 Service
 - Docker
 - Docker Testcontainers using MySQL

# Usage
Here is a **flowchart** of the *app's structure*:
![](/images/Task_Management_System_structure.png)

## Util Controllers
These are the Utility Controllers — they are used for actions like authentication, OAuth2 Authorization, user profile management, etc. Utility controllers are not used for any main features like project management.

### Authentication Controller

*This controller* is available for *everyone* — even for **non-authenticated** users. There is always a **default user** created during the initialization of the database. It will always have a special *OWNER* role and it's credentials can be configured.
***Available endpoints:***
 - **POST:** `/login` — **accepts** *user credentials* (email or username, password) and **returns** a *JWT* for authentication.
 - **POST**: `/register` — **accepts** *user details* (like name) and credentials. **Returns** a *newly created user*.

All inputted data is **verified** during these requests, so emails must follow the email pattern, and passwords must match. If request is invalid, **response code 400** will be thrown.

Some examples:

*Login request:*

![](/images/login.png)

Response will look something like this: `{"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvd25lckBib29rc3RvcmUuY29tIiwiaWF0IjoxNzA3MzI2ODg5LCJleHAiOjE3MDczMjg2ODl9.4_2yXaHkGMxtpi14Jzsvi9kET4Lis_OdxlAOnurys-ha6Bfn_t6vJnU1fD9DjCvmg1PGTq1a3_RiahhiHQ83PQ"}`

*Registration request:*

![](/images/registration_request.png)

*Registration response:*

![](/images/registration_response.png)

For all endpoints in other controllers authentication is **required**. If **no token** is sent with the requests, **response code 401** will be thrown.

### User Controller

*This controller* contains endpoints to *see* and *change* **users**.
***Available endpoints:***
 - **GET:** `/users/me` — **returns** current *user details*.
 - **PATCH:** `/users/{id}/roles` — **updates** *roles* for user. *Requires* **OWNER** role.
 - **PUT:** `/users/me` — **updates** current *user details*.
 - **GET:** `/users` — **lists** *all users*. Supports pagination. *Requires* **MANAGER** role.
 - **DELETE:** `/users/me` — **deletes** *current user*. *Requires* **confirmation** through user *credentials*.
 - **PATCH:** `/users/{id}/lock` — **locks** or **unlocks** the *specified user* account. *Requires* **MANAGER** role.

Some examples:

*My profile:*

![](/images/my_profile_response.png)

*Change user roles request:*

![](/images/change_user_project_role_request.png)

*Change user roles response:*

![](/images/change_user_project_role_response.png)

### OAuth2 Controller

*This controller* is responsible for **OAuth2 Authorization**. It includes authorization endpoints for each third-party service (currently *Dropbox* and *Google*) and *callback endpoint* that is a part of OAuth2 Flow and is not supposed to be called by users *directly*.
***Available endpoints:***
 - **GET:** `/oauth2/connect/dropbox` — **initiates** *Dropbox* **authorization** by *redirecting* to their authorization endpoint.
 - **GET:** `/oauth2/connect/google` — **initiates** *Google* **authorization** by *redirecting* to their authorization endpoint.
 - **GET:** `/oauth2/connect/code` — **is called** by authorization server in order to send the code. It is ***not supposed to be called by users***.

OAuth2 *tokens* which are used for third-party interactions have a **limited lifespan**. This is countered by using *refresh tokens* that either never expire or have significantly longer lifespan. Use of refresh tokens *is not mandatory* though and can be disabled in the app configuration file.

Some examples:

*Dropbox authorization redirect:*

![](/images/dropbox_redirect.png)

*Google authorization redirect:*

![](/images/google_redirect.png)

### Dropbox Controller

*This controller* contains some **Dropbox** specific methods.
***Available endpoints:***
 - **GET:** `/dropbox/test` — **tests** *Dropbox* connection using their test endpoint. *Returns* **success message**.
 - **DELETE:** `/dropbox/logout` — **logs out** of *Dropbox*. This means all *standard and refresh tokens* will be **revoked**. 

Logout for *Dropbox* currently has a *problem* — it can lead to **deadlocking** of some of the app's functions. This is the case because the current logout implementation very heavily relies on the fact that current **refresh token is valid**. *Dropbox* refresh tokens are supposed to be permanent and don't expire but if token is somehow *rendered invalid* then logout is **impossible** along with the entire set of *Dropbox* related features. **This will be fixed in the future**.

### Google Controller

*This controller* contains some **Google** specific methods.
***Available endpoints:***
 - **GET:** `/google/test` — **tests** basic *Google Calendar* functionality by **creating a calendar**, **creating an event in the calendar**, **deleting the event** and **deleting the calendar**. Takes *a while* to work out.
 - **DELETE:** `/google/logout` — **logs out** of *Google*. This means *all standard and refresh tokens* will be **revoked**.

*Google's* logout is more **flexible** then *Dropbox's*: it will *always logout* no matter whether the token can be revoked. *Google's* refresh tokens are not permanent, so this behaviour is paramount.

### Swagger UI

Using `/swagger-ui/index.html` endpoint, you can try out the server using pretty **Swagger UI**:

![](/images/swagger.png)

## Main Controllers
These are the *Main Controllers* — they contain endpoints for interaction with app's* main entities* like **projects**, **tasks** or **messages**.

Everything concerning the *projects* has its own **role system**. Every project has its members *divided* into **different categories**: **CONTRIBUTORS** and **ADMINS**. *Project creator* has their own role — **CREATOR**, and only one CREATOR can be in a project. These roles — which are **project-scope** — should not be confused with *USER*, *MANAGER* and *OWNER* roles which are **application-scope** roles. Just like with *application-scope* roles, *project-scope* roles are used in order to *restrict* some users from a certain functionality within a project.

Projects also have an *important property* which should be mentioned — **privacy**. Private projects cannot be seen by any users **outside** of the project itself. On the other hand, **public** projects are **visible to everyone**, allowing users outside a project to leave comments for example.

*Application MANAGERs* have the authority to **bypass all project security features**.

### Project Controller
*This controller* is used to interact with **projects**.

**Project status** is mostly set automaticaly based on the *start*, *end* and *current* **dates**. If the **start date** is **after** the **current date**, the status is set to **INITIATED**. If the **start date** is **before** the **current date** and **the end date** is **after** the **current date**, *in case it is specified*, the status is set to **IN_PROGRESS**. If **end date** *is specified* and is **before** the **current date**, the status is set to **OVERDUE**. *Project CREATOR* can declare status to be **COMPLETED**, when the project is done by calling `/projects/{projectId}/status` endpoint.

***Available endpoints:***
 - **GET:** `/projects/{id}` — **finds** a *project* with the *specified id*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/projects/me` — **finds** all *projects* that the current user is a **member** of.
 - **GET:** `/projects/search` — **finds** *all public projects* and *private projects* that the current user is a **member** of **filtered** by the **name** specified. *MANEGERs can see all projects*.
 - **POST:** `/projects` — **creates** a *new project*. If *starting date* is *not specified*, it is set to **current date**. If *privacy* is *not specified*, it is set to **public**. If the user has *Dropbox* connected, *Dropbox* is **enabled** for the project. If the user has *Google* connected, *Calendar* will be **enabled** for the project. *Be aware*, that if third-party services are enabled (especially both of them), it **will take some time** for the project to get created.
 - **PUT:** `/projects/{id}` — **updates** the *specified project*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** `/projects/{id}` — **deletes** the *specified project*. If *Dropbox* and/or *Google Calendar* is/are connected, then the *shared folder* and/or the *calendar* will be **deleted**. *Requires* the user to be at least **CREATOR**.
 - **POST:** `/projects/{projectId}/users/{userId}/add` — **adds** the *specified user* to the *specified project*. *Requires* the user to be at least **ADMIN**. The new user will be automaticaly given **CONTRIBUTOR** *project role*. If the project has *Dropbox* connected, the user will be *added* to the *shared folder*. If the project has *Calendar* connected, the user will be added to the *shared calendar*.
 - **DELETE:** `/projects/{projectId}/users/{userId}/remove` — **removes** the *specified user* from *project*. *Requires* the user to be at least **ADMIN**. **CREATOR** *cannot be removed from the project*. The user will be **removed** from *Dropbox* *shared folder* and the *calendar*, if those exist.
 - **DELETE:** `/projects/{id}/quit` — *current user* will **quit** the *specified project*. The user will be **removed** from *Dropbox* *shared folder* and the *calendar*, if those exist.
 - **PATCH:** `/projects/{projectId}/users/{userId}/roles` — **changes** the *project role* of the *specified user*. *Requires* the user to be at least **CREATOR**. If the *role specified* is **CREATOR**, then *current* **CREATOR** will be **demoted** to **ADMIN**. *Dropbox* *shared folder* and the *calendar* will be **transfered** to the new **CREATOR** as well, if those exist.
 - **PATCH:** `/projects/{projectId}/status` — **changes** the *project status*. Note that *available options* are only **IN_PROGRESS** and **COMPLETED**. *Requires* the user to be at least **CREATOR**.
 - **PATCH:** `/projects/{id}/dropbox/connect` — **connects** the *specified project* to *Dropbox* by creating the *shared folder* and **adding** *all members* there. *Requires* the user to be at least **CREATOR**. This action follows ***'rigid'*** *connection strategy*. That means that *all project members* will be **connected** and if some are *not able to connect* for whatever reason, the action will be **aborted**. This will likely be changed to 'flexible' connection strategy in the future.
 - **PATCH:** `/projects/{id}/calendar/connect` — **connects** the *specified project* to *Google Calendar* by **creating** *the calendar*, *project start and end date events* and **adding** *available members* to the *new calendar*. *Requires* the user to be at least **CREATOR**. This action follows ***'flexible'*** connection strategy. That means that there will be *an attempt* to **connect** *all users*, but if some *cannot connect*, then they will be **left behind** and can *connect later* using `/projects/{id}/calendar/join`.
 - **PATCH:** `/projects/{id}/calendar/join` — **join** the *existing calendar* in the *specified project*. *Requires* the user to be at least **CONTRIBUTOR**.

Some examples:

*My projects:*

![](/images/my_projects_response.png)

*Create project request:*

![](/images/create_project_request.png)

*Create project response:*

![](/images/create_project_response.png)

*Add user to project:*

![](/images/add_user_to_project_response.png)

*Change project role for user request:*

![](/images/change_user_project_role_request.png)

*Change project role for user response:*

![](/images/change_user_project_role_response.png)

### Task Controller
*This controller* is responsible for managing interaction with **tasks**.

**Task status** is mostly set *automaticaly*, just as the *project status*. When a new task is created, its **status** is set to **NOT_STARTED**. Task *can be started* by its **assignee** using a special `/tasks/{id}/status endpoint`. When the *assignee* have **completed** their task, they can set the status to **COMPLETED** using the *previously mentioned endpoint*. If *task's status* **was not set** to **COMPLETED** *before* its *due date*, status is set to **OVERDUE**, it is not important whether the task was started or not. 

***Available endpoints:***
 - **GET:** `/tasks/me` — **finds** all tasks *current user* is *assigned* to. Supports pagination.
 - **GET:** `/tasks/projects/{projectId}/users/{userId}` — **finds** *all tasks* user is *assigned* to in the *specified project*. Supports pagination. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/tasks/projects/{id}` — **finds** *all tasks* in the *specified project*. Supports pagination. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be *public*.
 - **GET:** `/tasks/{id}` — **finds** the *specified task*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/tasks/labels/{id}` — **finds** *all tasks* under the *specified label*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **POST:** `/tasks` — **creates** a *new task*. The task's **assignee**, *if not specified*, is set to the **current user**. *Requires* the user to be at least **ADMIN**. If the project is connected to *Dropbox*, *task folder* will be **created** in the *shared project folder*. If the project is connected to *Google Calendar* and *due date* is specified, **an event** for that task's **end day** will be **created**.
 - **PUT:** `/tasks/{taskId}` — **updates** the *specified task*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** /tasks/{taskId} — **deletes** the *specified task*. If *Dropbox* is connected, the *task's folder* will be **deleted**. If *Google Calendar* is connected and the *event exists*, it will be deleted. *Requires* the user to be at least **ADMIN**.
 - **PATCH:** `/tasks/{taskId}/status` — **changes** *task status*. Note that *available options* are only **IN_PROGRESS** and **COMPLETED**. *Requires* the user to be the **assignee** of *this task*.

Some examples:

*My tasks:*

![](/images/my_tasks_response.png)

*Create task request:*

![](/images/create_task_request.png)

*Create task response:*

![](/images/create_task_response.png)

*Change task status request:*

![](/images/change_task_status_request.png)

*Change task status response:*

![](/images/change_task_status_response.png)

### Label Controller
*This controller* is responsible for managing the **labels**.

**Label** is an *auxiliary entity* which is used to *better group tasks together*. **Many tasks** might be under **one label**, and a **task** might have **several labels** (many to many relationship).

***Available endpoints:***
 - **GET:** `/labels/projects/{projectId}` — **find** labels for the specified project. Requires the user to be at least CONTRIBUTOR or for the project to be public.
 - **GET:** `/labels/{labelId}` — **find** the *specified label*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **POST:** `/labels` — **create** a *new label*. *Requires* the user to be at least **ADMIN**.
 - **PUT:** `/labels/{labelId}` — **update** the *specified label*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** `/labels/{labelId}` — **delete** the *specified label*. *Requires* the user to be at least **ADMIN**.
  
Some examples:

*Create label request:*

![](/images/create_label_request.png)

*Create label response:*

![](/images/create_label_response.png)

*Task's label set change:*

![](/images/create_label_task_change.png)

### Message Controller
*This controller* is used to interact with **comments** and **replies**.

**Comments** can be left by users under a *certain task*. Other users can **respond** to *these comments* by leaving **replies** on them. **Replies** can *also* be **replied to**.

***Available endpoints:***
 - **GET:** `/messages/comments/tasks/{taskId}` — **find** all *comments* for the *specified task*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/messages/comments/projects/{projectId}` — **find** all *comments* for the *specified project*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/messages/comments/{commentId}` — **find** the *specified comment*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/messages/replies/{replyId}` — **find** the *specified reply*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/messages/comments/{commentId}/replies` — **find** all *replies* for the *specified comment*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **POST:** `/messages/comments/tasks/{taskId}` — **leave** a *comment* under the *specified task*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **POST:** `/messages/{messageId}/replies` — **leave** a *reply* under the *specified message*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **PUT:** `/messages/{messageId}` — **update** a *message*. *Requires* for user to be the **creator of the comment**. When a *message* gets **updated**, a **date of update** is *shown* along with the **date of creation**.
 - **DELETE:** `/messages/{messageId}` — **deletes** a *message*. *Requires* for user to be the **creator of the comment**.
  
Some examples:

*Leave comment request:*

![](/images/leave_comment_request.png)

*Leave comment response:*

![](/images/leave_comment_response.png)

*Reply to comment request:*

![](/images/reply_request.png)

*Reply to comment response:*

![](/images/reply_response.png)

*Reply to reply request:*

![](/images/reply_to_reply_request.png)

*Reply to reply response:*

![](/images/reply_to_reply_response.png)

*Project comments:*

![](/images/project_comments_response.png)

*Replies for comment:*

![](/images/comment_replies_response.png)

### Attachment Controller
*This controller* is responsible for managing **attachments**. These *attachments* represent **files** that can be attached to **tasks** and *shared* with other users through *Dropbox*.

Quite unsurprisingly, in order to work, **attachments** require *Dropbox connection*. If *Dropbox* is **not connected** to the *project*, **attachments** will **not be available**.

***Available endpoints:***
 - **GET:** `/attachments/tasks/{taskId}` — **find** all *attachments* for the *specified task*. *Requires* the user to be at least **CONTRIBUTOR**.
 - **POST:** `/attachments/tasks/{taskId}` — **creates** a new *attachment* by **uploading** a file to *Dropbox*. *Requires* the user to be at least **CONTRIBUTOR**.
 - **GET:** `/attachments/{attachmentId}` — **downloads** a *file* in this *attachment* from *Dropbox*. *Requires* the user to be at least **CONTRIBUTOR**.

Some examples:

*File upload request:*

![](/images/file_upload_request.png)

*File upload response:*

![](/images/file_upload_response.png)

# Configuration
*To configure* the **server** you might need to **change** a *few files*, such as:
1. `application.properties` — sets several **crucial variables** for *Spring Framework* to use. Also provides settings for **JWT** and **OAuth2**. If *Docker* is used, application settings should be set in **.env** file **instead**.
2. `liquibase.propeties` — **required** to change only when *Docker* is **not used**. Sets database connection properties.
3. `11-insert-owner.yaml` — you need to change this file to set your own **credentials** for the *main user*. Without changing these, **security** of your server may be **compromised**.
4. `.env` — if you are using *Docker*, you should set **application variables** in **here** *instead* of in *application.properties*.

## application.properties

*Path to the file:*

![](/images/application.properties_path.png)

*These values* should be set only in the case where *Docker* is **not used**. If you are using *Docker*, these parameters should be set in `.env` file. This likely will be *fixed later*, so that only `.env` file will be required to be configured.

 - `spring.datasource.url` — sets **database URL** to connect to.
 - `spring.datasource.username` — sets **username** for *database* user.
 - `spring.datasource.password` — sets **password** for *database*.
 - `spring.datasource.driver-class-name` — sets **driver** for your *database*. In case of **MySQL**: `com.mysql.cj.jdbc.Driver`
 - `jwt.expiration` — determines *how long* one **token** will be **active** for in *milliseconds*. 
 - `jwt.secret` — sets the **secret key** used in forming **tokens**. The key is supposed to be *quite long*.
 - `dropbox.root.path` — sets a **default path** to *Dropbox shared folder*.
 - `oauth2.providers` — sets **OAuth2 Providers**. Their *names* need to be *separated* by `,`. In the current app version, there are only **two**: `dropbox` and `google`.
 - `oauth2.<provider>.client-id` — sets **client id** for *provider*.
 - `oauth2.<provider>.client-secret` — sets **client secret** for *provider*.
 - `oauth2.<provider>.redirect-uri` — sets **redirect uri**. In the current app version, this parameter **must be set** to `<host>/oauth2/connect/code`, where `<host>` is the **current server address**.
 - `oauth2.<provider>.authorization-uri` — sets provider's **OAuth2 Authorization endpoint** address.
 - `oauth2.<provider>.token-uri` — sets provider's **authorization code exchange endpoint** address.
 - `oauth2.<provider>.scope` — sets **OAuth2 scopes**. Default values are *already* set. 
 - `oauth2.<provider>.use-refresh-tokens` — whether the *OAuth2 Service* should use **refresh tokens** for this provider. `true` value is *recommended*.

*Example:*

![](/images/application.properties_example.png)

## liquibase.properties

*Path to the file:*

![](/images/liquibase.properties_path.png)

Change only if *Docker* is **not used**, otherwise nothing will happen:

 - `url` — sets **database URL** to connect to.
 - `username` — sets **username** for *database* user.
 - `password` — sets **password** for *database* user.

*Example:*

![](/images/liquibase.properties_example.png)

## 11-insert-owner.yaml

*Path to the file:*

![](/images/insert_owner_path.png)

You can **change** *any of the values* which are shown on the **screenshot below**, but the most *important* ones are **email**, **username** and **password**. You will use these *credentials* to **login into** the *main admin account*.
*Password* must be **encrypted** first, you can use *any online service* that **hashes** text using **BCrypt algorithm** (10 rounds). *For example*, [this](https://www.browserling.com/tools/bcrypt) one.

After the *initial* creadential values are set and you have **logged in**, they can be *altered* using `/users/me` **PUT** *endpoint*.

*Example:*

![](/images/insert_owner_example.png)

## .env

*Path to the file:*

![](/images/.env_path.png)

This file sets **variables** for *Docker* **virtual environment**. Some if these are *not necessary* to alter, even if you are using *Docker*.

These can be left as they are:
 - `MYSQL_PASSWORD` — **password** for *MySQL* database.
 - `MYSQL_DATABASE` — **name** for the *database*.
 - `MYSQL_LOCAL_PORT` — **port**, on which the *database* will be available for **access**.
 - `MYSQL_DOCKER_PORT` — **port**, on which the *database* is running **inside** *Docker*.
 - `SPRING_LOCAL_PORT` — **port**, on which the *server* will be available for **access**.
 - `SPRING_DOCKER_PORT` — **port**, on which the *server* is running **inside** *Docker*.
 - `DEBUG_PORT` — **port**, on which remote **debug function** is available.

*Everything* concerning **OAuth2** and **JWT** authentication is *mandatory*:

 - `OAUTH2_PROVIDERS` — **the same as** `oauth2.providers` in `application.properties`.

*Dropbox:*

 - `DROPBOX_CLIENT_ID` — **the same as** `oauth2.dropbox.client-id` in `application.properties`.
 - `DROPBOX_CLIENT_SECRET` — **the same as** `oauth2.dropbox.client-secret` in `application.properties`.
 - `DROPBOX_REDIRECT_URI` — **the same as** `oauth2.dropbox.redirect-uri` in `application.properties`.
 - `DROPBOX_AUTH_URI` — **the same as** `oauth2.dropbox.authorization-uri` in `application.properties`.
 - `DROPBOX_TOKEN_URI` — **the same as** `oauth2.dropbox.token-uri` in `application.properties`.
 - `DROPBOX_SCOPE` — **the same as** `oauth2.dropbox.scope` in `application.properties`.
 - `DROPBOX_REFRESH_TOKENS` — **the same as** `oauth2.dropbox.use-refresh-tokens` in `application.properties`.
 - `DROPBOX_SHARED_FOLDER_ROOT_PATH` — **the same as** `dropbox.root.path` in `application.properties`.

*Google:*

 - `GOOGLE_CLIENT_ID` — **the same as** `oauth2.google.client-id` in `application.properties`.
 - `GOOGLE_CLIENT_SECRET` — **the same as** `oauth2.google.client-secret` in `application.properties`.
 - `GOOGLE_REDIRECT_URI` — **the same as** `oauth2.google.redirect-uri` in `application.properties`.
 - `GOOGLE_AUTH_URI` — **the same as** `oauth2.google.authorization-uri` in `application.properties`.
 - `GOOGLE_TOKEN_URI` — **the same as** `oauth2.google.token-uri` in `application.properties`.
 - `GOOGLE_SCOPE` — **the same as** `oauth2.google.scope` in `application.properties`.
 - `GOOGLE_REFRESH_TOKENS` — **the same as** `oauth2.google.use-refresh-tokens` in `application.properties`.

*Example:*

![](/images/.env_example.png)

# Installation
First you need to compile the project. You must have **Docker**, **Maven** and **JDK 21** installed on your device in order for this to work.
1. Download the repository and unzip it into a directory.
2. Configure **all files** as it was shown in the previous section.
3. If you want to launch the server using **Docker**, then you can **skip** this step. Otherwise, you need to **remove Docker dependency** so that application starts without it.

*pom.xml is in the root directory:*

![](/images/remove_docker_dependancy.png)

1. Open terminal in the project's directory.
2. Use `mvn clean package` in order to build the project. **You must have Docker opened during this process regardless of whether you want to use it in the end or not**, since testcontainers will be created.

When all tests pass and the project is compiled, you can **launch** the server.

If you are using **Docker**:
Use `docker-compose build` to create a container. After that is done, you can use `docker-compose up` or **Docker app** to launch it.
Server will be accessible at port, specified in the `.env` file.

If you are launching **without Docker**:
**Ensure that all files are configured properly and the database is running**. After that use `java -jar jv-task-management-system-0.0.1-SNAPSHOT.jar` in terminal opened at `<project_folder>/target` directory. Server will be available for access at default `8080` port.

# Issue
Although most of the features are implemented, the project is still in **development**. If you found some bug or problem, please [raise an issue](https://github.com/ItsUnbidden/jv-task-management-system/issues).

Known problems:

 - There is currently **a problem** with *error codes* — **401 error code** and usually incorrect message are returned in cases where there is supposed to be a *different exception response*. This might create some confusion when identifying the cause of the issue. I haven't been able to find a solution for this so it's going *to stay* for now.
 - **Logging out** of *Docker* might **not be possible** under certain conditions. One of those *conditions* is when the **access token** has been rendered *invalid*.
 - *Google Calendar* and *Dropbox* **Services** are implemented very **differently**, so there might be some **inconsistencies** between them. This is because they were written at *different times* and with different *amount* of **experience**. I consider *Google Calendar Service* to be superior and *Dropbox Service* might be **updgraded** to it's level later.
 - **Only about 40%** of the *application* is *covered* with **tests**. This is due to the fact, that a *significant amount* of app's **code** is related to **OAuth2**, **Google Calendar** and **Dropbox**. These services are **very hard** to **test**, since they require *connection* to some *external resource* and 'logging in' an *external account* (API Console). For now I **wasn't able** to find a way to **test** them, and so they *lower the overall coverage severely*.
