**Dormitory Management System (JavaFX + MySQL)**

A JavaFX desktop app for managing AAU dormitory applications, approvals, announcements, messaging, and allocations. It significantly reduce in-person activity frictions and boost efficiency.

---

## 1) App features summary

### Authentication + Roles
- Role-based login: **Student**, **Admin**, **Owner**
- Basic input validation and error handling on login/forms

### Student side
- Register / login (student-style ID format support)
- Submit dorm application in **phases** (Phase 1 → Phase 2 workflow)
- View announcements
- Send/receive messages with staff
- View application status + assigned building info (when allocated)

### Admin side
- View and manage student applications (approve/decline/request resubmission)
- Filter/sort applications by common attributes (college, gender, residency, etc.)
- Allocate/assign students to buildings
- Post/edit/delete announcements
- Message students and track read status
- Export selected student records to **CSV**

### Owner side
- Everything Admin can do
- Manage staff accounts (admin/owner-level management)

---

## 2) How to run (IntelliJ, JDK 23 + manual JavaFX setup)

### A) Install JDK 23
1. Install **JDK 23**
2. In IntelliJ: **File → Project Structure → Project**
   - Project SDK: **JDK 23**
   - Project language level: **23**

### B) Add JavaFX SDK as a Library
1. Download OpenJFX SDK for Windows (example we used: `javafx-sdk-25.0.2`)
2. IntelliJ: **File → Project Structure → Libraries**
3. Click **+ → Java**
4. Select the JavaFX SDK `lib` folder, for example:
   - `C:\Users\Test\Downloads\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib`

> Make sure the library is attached to your project/module.

### C) Add VM options for the Run Configuration (App.java)
1. Open **Run → Edit Configurations**
2. Select the configuration that runs `dorm.App`
3. Enable **VM options**
4. Add this (adjust paths to your JavaFX SDK location):

```text
--module-path "C:\Users\Test\Downloads\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib" --add-modules javafx.controls,javafx.fxml
````

### D) Setup MySQL database

1. Start MySQL Server
2. Run the schema script:

```sql
-- From MySQL CLI:
SOURCE sql/schema.sql;
```

Or via command line:

```bash
mysql -u root -p < sql/schema.sql
```

### E) Configure DB connection used by the app

Edit:

`src/main/resources/dorm/db.properties`

Set your MySQL credentials, for example:

```properties
db.url=jdbc:mysql://localhost:3306/dormitory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_PASSWORD
```

### F) Run

Run:

`src/main/java/dorm/App.java`


## 3) Default credentials (for testing)

**Admin**

  * Username: `admin`
  * Password: `admin123`

**Owner**

  * Username: `owner`
  * Password: `owner123`
