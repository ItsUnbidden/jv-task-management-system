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
  - [Main Controllers](#main-controllers)
    - [Project Controller](#project-controller)
    - [Task Controller](#task-controller)
    - [Subtask Controller](#subtask-controller)
    - [Label Controller](#label-controller)
    - [Message Controller](#message-controller)
    - [Attachment Controller](#attachment-controller)
- [Configuration](#configuration)
  - [application-prod.properties](#application-prodproperties)
  - [.env](#env)
  - [Environmental variables](#environmental-variables)
- [Installation](#installation)
- [Issue](#issue)
- [License](#license)

# Introduction
**The goal** of this project was to create a **Java server** for an application that can be used by developers working together on **large projects** which require **complex administration**. Currently, this backend is supposed to be paired with an **Angular frontend** which you can find [here](https://github.com/ItsUnbidden/angular-task-management-system). Here are some of the project's key features:
 - Authentication using **JWT** and refresh token cookies
 - Application wide **role system**, allowing admins to manage the app
 - A system for browsing and managing projects with features such as:
    - **Public** and **private** projects — public projects can be seen by logged-in users
    - **Comments and replies system** for tasks in projects
    - Tasks can be **assigned** to project members
    - Users can create **subtasks** for better organisation
    - Progress of **tasks** and **projects** is expressed with a *0 to 100* value depending on how many **tasks** and **subtasks** are completed
    - Both tasks and projects have **date bounds** that influence their status
    - many different **sorting methods** for tasks, projects, etc.
 - User management for admins
 - Projects can be connected to *Dropbox* in order to enable **file sharing** within a project
 - Projects can also be connected to *Google Calendar* in order to better **track progress** and get **notifications**
 - *Third party services* are connected with **OAuth2** protocol
 - *Swagger UI* for easier integration into other projects and trying out the server **(currently not being updated due to its redundancy because of the frontend)**

**Since the backend is being actively revamped, this README is significantly outdated in some places. I will eventually update it, but be aware that some things are actually quite different from what is described here.**

Some additional notes about recent updates:
 - **Dropbox** integration has been nearly *completely revamped*. It is now truly optional and can't break the main functionality of the app even if something goes wrong.
 - **Calendar** is now in a *non-functional* state due to all of the external service subsystem upgrades. I'm considering *deprecating* it due to the numerous issues it has and the limited bonuses it gives. Perhaps I will try to implement some kind of *notification system* instead.
 - **Subtasks** have been *added*.
 - **Projects** and **tasks** now have a *progress* value. For **tasks**, it depends on the number of *completed* **subtasks** in that specific **task**. For **projects**, it's calculated based on the current *progress* of all the **tasks** in the **project**.

# Technologies
The project is built using mainly the **Spring Framework**. Here is a complete list of technologies employed:
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
Here I will describe how the server **features** work *in detail*.

## Util Controllers
These are the Utility Controllers — they are used for actions like authentication, OAuth2 authorization, user profile management, etc.

### Authentication Controller
*Most endpoints* in this controller are available for *everyone* — even for **non-authenticated** users. There is always a **default user** created during the initialization of the database. They will always have a special *OWNER* role. All of these endpoints start with `/auth`.

After you start the app for the first time, you should log in as the **owner** and change its *credentials* using `/users/me`.

***Available endpoints:***
 - **POST:** `/login` — **accepts** *user credentials* (email or username, password) and **sets** HTTP-only *JWT* and refresh token cookies for authentication.
 - **POST:** `/register` — **accepts** *user details* and credentials. **Returns** a *newly created user*.
 - **POST:** `/refresh` — **refreshes** the current *short-lived* token. It also rotates the *long-lived* token. Doesn't return a body, but sets the **cookies**.
 - **DELETE:** `/logout` — **invalidates** the current *long-lived* token and **removes** all *authentication cookies*. This endpoint needs a valid *short-lived* token.
 - **GET:** `/csrf` — **forces** the resolution of the *CSRF token*. If there's no *CSRF token*, its cookie will be set.
 - **GET:** `/csrf/refresh` — **refreshes** the *CSRF token*. Should be used after *login* and *logout*.

All input data is **verified** during these requests, so emails must follow the email pattern, and passwords must match.

Some examples:

*Login request:*

![](/images/login.png)

Response will include the cookies for the short-lived and long-lived tokens.

*Registration request:*

**Note: first and last names do not exist anymore. This image will be updated later.**

![](/images/registration_request.png)

*Registration response:*

![](/images/registration_response.png)

For all endpoints in other controllers, authentication is **required**. Also, a **CSRF token** is mandatory for all state-altering requests.

If you're interested in how this authentication flow works in practice, you should check out the [frontend](/https://github.com/ItsUnbidden/angular-task-management-system).

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
*This controller* is responsible for **OAuth2 Authorization**. It includes authorization endpoints for each third-party service (currently *Dropbox* and *Google*) and a *callback endpoint* that is a part of the OAuth2 Flow and is not supposed to be called by users *directly*.

***Available endpoints:***
 - **GET:** `/oauth2/connect/dropbox` — **initiates** *Dropbox* **authorization** by *redirecting* to their authorization endpoint.
 - **GET:** `/oauth2/connect/google` — **initiates** *Google* **authorization** by *redirecting* to their authorization endpoint.
 - **GET:** `/oauth2/connect/code` — **is called** by authorization server in order to send the code. It is ***not supposed to be called by users***.

OAuth2 *tokens*, which are used for third-party interactions, have a **limited lifespan**. This is countered by using *refresh tokens* that either never expire or have a significantly longer lifespan. Use of refresh tokens *is not mandatory* though and can be disabled in the app configuration file.

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
 - **GET:** `/dropbox/status` — **gets** the current status of *Dropbox*. Possible values are: `OK`, `EXPIRED`, or `NOT_CONNECTED`.

### Google Controller
*This controller* contains some **Google** specific methods.

***Available endpoints:***
 - **GET:** `/google/test` — **tests** basic *Google Calendar* functionality by **creating a calendar**, **creating an event in the calendar**, **deleting the event** and **deleting the calendar**. Takes *a while* to work out.
 - **DELETE:** `/google/logout` — **logs out** of *Google*. This means *all standard and refresh tokens* will be **revoked**.
 - **GET:** `/google/status` — **gets** the current status of *Google Calendar*. Possible values are: `OK`, `EXPIRED`, or `NOT_CONNECTED`.

*Google integration* is currently almost *non-functional* due to all of the recent revamps. I’m currently considering **removing its support entirely**.

## Main Controllers
These are the *Main Controllers* — they contain endpoints for interaction with the app's *main entities* like **projects**, **tasks**, and/or **messages**.

Everything concerning the *projects* has its own **role system**. Every project has its members *divided* into **different categories**: **CONTRIBUTORS** and **ADMINS**. *Project creator* has their own role — **CREATOR**, and only one CREATOR can be in a project. These roles — which are **project-scope** — should not be confused with the *USER*, *MANAGER*, and *OWNER* roles, which are **application-scope** roles. Just like with *application-scope* roles, *project-scope* roles are used in order to *restrict* users from a certain functionality within a project.

Projects also have an *important property* which should be mentioned — **privacy**. Private projects cannot be seen by any users **outside** of the project itself. On the other hand, **public** projects are **visible to everyone**, allowing users outside a project to leave comments, for example.

*Application MANAGERs* have the authority to **bypass all project security features**.

### Project Controller
*This controller* is used to interact with **projects**.

**Project status** is mostly set automatically based on the *start*, *end* and *current* **dates**. If the **start date** is **after** the **current date**, the status is set to **INITIATED**. If the **start date** is **before** the **current date** and **the end date** is **after** the **current date**, *in case it is specified*, the status is set to **IN_PROGRESS**. If **end date** *is specified* and is **before** the **current date**, the status is set to **OVERDUE**. *Project CREATOR* can declare status to be **COMPLETED** when the project is done by calling the `/projects/{projectId}/status` endpoint.

**Project progress** mostly depends on the progress of the project's **tasks**. If none of the tasks have any progress, the project's *progress* will be **0 (min)** unless the status is **COMPLETED**, in which case, the *progress* will always be **100 (max)**. That means the **COMPLETED** status value always **overrides** the current *progress* with the *maximum possible value*.

***Available endpoints:***
 - **GET:** `/projects/{id}` — **finds** a *project* with the *specified id*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/projects/me` — **finds** all *projects* that the current user is a **member** of.
 - **GET:** `/projects/search` — **finds** *all public projects* and *private projects* that the current user is a **member** of **filtered** by the **name** specified. *MANAGERS can see all projects*.
 - **POST:** `/projects` — **creates** a *new project*. If *starting date* is *not specified*, it is set to **current date**. If *privacy* is *not specified*, it is set to **public**. If the user has *Dropbox* connected, *Dropbox* is **enabled** for the project. If the user has *Google* connected, *Calendar* will be **enabled** for the project. *Be aware*, that if third-party services are enabled (especially both of them), it **will take some time** for the project to get created.
 - **PUT:** `/projects/{id}` — **updates** the *specified project*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** `/projects/{id}` — **deletes** the *specified project*. If *Dropbox* and/or *Google Calendar* is/are connected, then the *shared folder* and/or the *calendar* will be **deleted**. *Requires* the user to be at least **CREATOR**.
 - **POST:** `/projects/{projectId}/users/{userId}/add` — **adds** the *specified user* to the *specified project*. *Requires* the user to be at least **ADMIN**. The new user will be automatically given the **CONTRIBUTOR** *project role*. If the project has *Dropbox* connected, the user will be *added* to the *shared folder*. If the project has *Calendar* connected, the user will be added to the *shared calendar*.
 - **DELETE:** `/projects/{projectId}/users/{userId}/remove` — **removes** the *specified user* from *project*. *Requires* the user to be at least **ADMIN**. **CREATOR** *cannot be removed from the project*. The user will be **removed** from *Dropbox* *shared folder* and the *calendar*, if those exist.
 - **DELETE:** `/projects/{id}/quit` — *current user* will **quit** the *specified project*. The user will be **removed** from *Dropbox* *shared folder* and the *calendar*, if those exist.
 - **PATCH:** `/projects/{projectId}/users/{userId}/roles` — **changes** the *project role* of the *specified user*. *Requires* the user to be at least **CREATOR**. If the *role specified* is **CREATOR**, then *current* **CREATOR** will be **demoted** to **ADMIN**. *Dropbox* *shared folder* and the *calendar* will be **transfered** to the new **CREATOR** as well, if those exist.
 - **PATCH:** `/projects/{projectId}/status` — **changes** the *project status*. Note that *available options* are only **IN_PROGRESS** and **COMPLETED**. *Requires* the user to be at least **CREATOR**.
 - **PATCH:** `/projects/{projectId}/dropbox/connect` — **connects** the *specified project* to *Dropbox* by creating the *shared folder* and **adding** *all members* there. *Requires* the user to be at least **CREATOR**.
 - **PATCH:** `/projects/{projectId}/calendar/connect` — **connects** the *specified project* to *Google Calendar* by **creating** *the calendar*, *project start and end date events* and **adding** *available members* to the *new calendar*. *Requires* the user to be at least **CREATOR**.
 - **PATCH:** `/projects/{projectId}/dropbox/join` — **join** an *existing project folder* in the *specified project*. *Requires* the user to be at least **CONTRIBUTOR**.
 - **PATCH:** `/projects/{projectId}/calendar/join` — **join** an *existing calendar* in the *specified project*. *Requires* the user to be at least **CONTRIBUTOR**.
 - **DELETE:** `/projects/{projectId}/dropbox/disconnect` — **disconnects** the *specified project* from *Dropbox* by **deleting** *the shared folder*. *Requires* the user to be at least **CREATOR**. If the *project folder* cannot be deleted due to an **external error**, the project will still be *disconnected* to prevent the inconsistency between the local and external states from breaking the related features.
 - **DELETE:** `/projects/{projectId}/google/disconnect` — **disconnects** the *specified project* from *Google Calendar* by **deleting** *the calendar*. *Requires* the user to be at least **CREATOR**.
 - **GET:** `/projects/{projectId}/progress` — **gets** the *current project progress*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.

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

**Task status** is mostly set *automaticaly*, just as the *project status*. When a new task is created, its **status** is set to **NOT_STARTED**. Task *can be started* by its **assignee** using a special `/tasks/{id}/status endpoint`. When the *assignee* **completes** their task, they can set the status to **COMPLETED** using the *previously mentioned endpoint*. If *task's status* **was not set** to **COMPLETED** *before* its *due date*, status is set to **OVERDUE**, it is not important whether the task was started or not.

**Task progress** mostly depends on the number of completed **subtasks**. If there are *no completed subtasks*, the *progress* will be **0 (min)** unless the status of the task is **COMPLETED**, in which case, the *progress* will always be **100 (max)**. That means the **COMPLETED** status value always **overrides** the current *progress* with the *maximum possible value*.

***Available endpoints:***
 - **GET:** `/tasks/me` — **finds** all tasks *current user* is *assigned* to. Supports pagination.
 - **GET:** `/tasks/projects/{projectId}/users/{userId}` — **finds** *all tasks* user is *assigned* to in the *specified project*. Supports pagination. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/tasks/projects/{id}` — **finds** *all tasks* in the *specified project*. Supports pagination. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be *public*.
 - **GET:** `/tasks/{id}` — **finds** the *specified task*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/tasks/labels/{id}` — **finds** *all tasks* under the *specified label*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **GET:** `/tasks/projects/{projectId}/filter` — **finds** *all tasks* using a [filter](/src/main/java/com/unbidden/jvtaskmanagementsystem/dto/task/specification/TaskFilterDto.java). Supports pagination. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
 - **POST:** `/tasks` — **creates** a *new task*. The task's **assignee**, *if not specified*, is set to the **current user**. *Requires* the user to be at least **ADMIN**. If the project is connected to *Dropbox*, the *task folder* will be **created** in the *shared project folder*. If the project is connected to *Google Calendar* and *due date* is specified, **an event** for that task's **end day** will be **created**.
 - **PUT:** `/tasks/{taskId}` — **updates** the *specified task*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** `/tasks/{taskId}` — **deletes** the *specified task*. If *Dropbox* is connected, the *task's folder* will be **deleted**. If *Google Calendar* is connected and the *event exists*, it will be deleted. *Requires* the user to be at least **ADMIN**.
 - **PATCH:** `/tasks/{taskId}/status` — **changes** *task status*. Note that *available options* are only **IN_PROGRESS** and **COMPLETED**. *Requires* the user to be the **assignee** of *this task*.
 - **GET:** `/tasks/{taskId}/progress` — **gets** the *task's progress*. Note that *available options* are only **IN_PROGRESS** and **COMPLETED**. *Requires* the user to be the **assignee** of *this task*.

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

### Subtask Controller
*Through this controller*, the users can interract with **subtasks**.

**Subtasks** are simple entities containing only a *title* and a *completion flag*. They allow the users to additionally *split* a **task** into several *smaller pieces*.
Marking a **subtask** as *completed* results in an increase to the **parent task's** *progress* (and subsequently the **project's**).

***Available endpoints:***
 - **GET:** `/subtasks/task/{taskId}` — **find** *subtasks* for the *specified task*. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**. Supports pagination.
 - **POST:** `/subtasks` — **create** a *new subtask*. *Requires* the user to be at least **ADMIN**.
 - **PUT:** `/subtasks/{subtaskId}` — **update** the *specified subtask*. *Requires* the user to be at least **ADMIN**.
 - **DELETE:** `/subtasks/{subtaskId}` — **delete** the *specified subtask*. *Requires* the user to be at least **ADMIN**.

### Label Controller
*This controller* is responsible for managing the **labels**.

**Label** is an *auxiliary entity* which is used to *better group tasks together*. **Many tasks** might be under **one label**, and a **task** might have **several labels** (many to many relationship).

***Available endpoints:***
 - **GET:** `/labels/projects/{projectId}` — **find** *labels* for the specified project. *Requires* the user to be at least **CONTRIBUTOR** or for the project to be **public**.
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

Quite unsurprisingly, **attachments** require a *Dropbox connection* to work. If *Dropbox* is **not connected** to the *project*, **attachments** will **not be available**.

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
*To configure* the **server** you will have to **change** these files:
1. `application-prod.properties` — contains **settings** for some of the app's features. This file is **not used** *by default*.
2. `.env` — defines **environmental variables** for *Docker Compose*.

If you do not use *Docker Compose*, you will have to also set **environmental variables**. How you do that depends on what your setup is.

## [`application-prod.properties`](/src/main/resources/application-prod.properties)

This file contains some useful app settings:

- `logging.level.com.unbidden.jvtaskmanagementsystem` — *app-specific* logging level. For **production**, `INFO` or `WARN` is recommended.
- `oauth2.dropbox.use-refresh-tokens` — whether **Dropbox** should use *refresh tokens*. `True` is recommended.
- `oauth2.google.use-refresh-tokens` — whether **Google Calendar** should use *refresh tokens*. `True` is recommended.
- `jwt.expiration` — defines how long **access tokens** will be considered *valid*. Must be specified in **milliseconds**.
- `jwt.refresh-expiration-hours` — defines how long **refresh tokens** will be considered *valid*. Must be specified in **hours**.
- `refresh-token.cleanup.interval` — defines the time interval between **refresh token** *cleanup cycles*. Must be specified in **hours**.
- `refresh-token.cleanup.initial-delay` — defines the initial **delay** before the first **refresh token** *cleanup cycle* after startup. Can be 0. Must be specified in **hours**.
- `refresh-token.cleanup.max-age-days` — defines for how long a **refresh token** is *stored*. After this time passes, the token will be **eligible for cleanup**. Must be specified in **days**.
- `frontend.base-url` — the *base URL* of the **frontend**. For example: `https://taskmanagementsystem.com`.
- `cors.allowed-origin-patterns` — A *whitespace-separated* list of allowed **request origins**. You might be required to specify the *frontend's base URL* here if you're getting **403 "Invalid CORS Request"** errors.
- `dropbox.root.path` — the path to the *folder* in the **users' Dropbox accounts** where shared *project folders* will be created. **Nesting folders is not recommended**. Example: `/TMS`.

This file **won't load** automatically by itself, because, by default, `application-dev.properties` is used, which is **not present** in the repository. You either have to add it or set a special *environmental variable*. The **instructions** on how to do that are in the section **below**.

## `.env`

This file sets **environmental variables** for *Docker Compose*. You do not need to change this file unless you're running the app using *Docker Compose*. The file is **not present** in the repository by default, so you will have to **create** it. You can use [`.env.sample`](/.env.sample) as a *template*.

## Environmental variables

If you do not use `Docker Compose`, you will have to set the **environmental variables** for sensitive parameters. How you do it depends on your system and case. For instance, you can configure the environment for **VS Code** launch configurations in `launch.json`:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "JvTaskManagementSystemApplication",
            "request": "launch",
            "mainClass": "com.unbidden.jvtaskmanagementsystem.JvTaskManagementSystemApplication",
            "projectName": "jv-task-management-system",
            "env": {
                "DB_BASE_URL": "localhost:3306",
                "DB_NAME": "task_management",
                "DB_USERNAME": "root",
                "DB_PASSWORD": "...",
                "DROPBOX_CLIENT_ID": "...",
                "DROPBOX_SECRET": "...",
                "GOOGLE_CLIENT_ID": "...",
                "GOOGLE_SECRET": "...",
                "JWT_SECRET": "..."
            }
        }
    ]
}
```

The list of the **variables** that have to be set can be found in [`.env.sample`](.env.sample).

Also, if you want to use the **production profile** of `application.properties`, you need to set an *additional variable*: `SPRING_PROFILES_ACTIVE=prod`. The repository does not contain `application-dev.properties` by default, so unless you **created one**, you would have to set this flag to **run the app**.

# Installation
First, you need to compile the project. You must have **JDK 25** and **Maven** installed on your device for this to work.
1. Download the repository and unzip it into a directory.
2. Configure **all files** as shown in the previous section.
3. In case you want the frontend to be served from the Spring backend (which is how it's supposed to work), you will also need to build it and copy all of the compiled files into the [static resource folder](/src/main/resources/static/). For more information, check out the [frontend repository](https://github.com/ItsUnbidden/angular-task-management-system).
4. Open a terminal in the project's directory.
5. Use `mvn clean package -DskipTests` to build the project. This will not run the test cases because they are currently broken. When the project is compiled, you can **launch** the server.

### If you are *using* Docker:

Use `docker-compose build` to create the required images. After that is done, you can use `docker-compose up` or the **Docker app** to launch it. The app will be accessible at the `8081` port.

### If you are launching without Docker:

**Ensure that all files are configured properly and the database is running**. After that, use `java -jar <name>.jar` (replace <name> with the actual name of the file that got generated) in a terminal opened at the `<project_folder>/target` directory. The server will be available for access at the default `8080` port. *A disclaimer*: this will **not work** unless you have all of the **environmental variables** set up correctly on your machine. If you don't want to set them, I'd recommend using *Docker Compose*.

# Issue
Although most of the features are implemented, the project is still in **development**. If you found a bug or problem, please [raise an issue](https://github.com/ItsUnbidden/jv-task-management-system/issues).

# License
This project is licensed under the [MIT License](/LICENSE).
