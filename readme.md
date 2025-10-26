# 🧾 SmartBoard

> **SmartBoard** — A simple digital noticeboard application (SEM 3 AJT Mini Project)

A **desktop Java application** that replaces paper noticeboards with a **priority-driven digital board**.  
Built using **Java Swing (UI)**, **Hibernate (ORM)**, and **MySQL (database)**.

---

## 👥 Contributors

- **Jaymin Dattani**
- **Ruchit Doshi**
- **Aditya Mevcha**

**Course / Instructor:** SEM 3 AJT Project — *Professor Dr. Ravi Kumar Natarajan*  

---

## 📑 Table of Contents

1. [Project Overview](#project-overview)  
2. [Features](#features)  
3. [Technology Stack](#technology-stack)  
4. [High-Level Architecture & Flow](#high-level-architecture--flow)  
5. [Repository Structure](#repository-structure)  
6. [Database Schema](#database-schema)  
7. [Configuration](#configuration)  
8. [Build & Run](#build--run)  
9. [JAR Files — Purpose & Explanation](#jar--lib-files--purpose--explanation)  
10. [Clone / Import to IDE](#how-to-clone--import-to-ide)  
11. [Usage — Admin & User Panels](#usage-admin--user-panels)  
12. [Troubleshooting & Common Issues](#troubleshooting--common-issues)  
13. [Future Work / Improvements](#future-work--improvements)  
14. [License & Acknowledgements](#license--acknowledgements)

---

## 🧩 Project Overview

**SmartBoard** is a digital desktop noticeboard system designed to **replace physical paper boards** in universities.  

It offers:
- A **User Panel** where students or visitors can view current notices.
- An **Admin Panel** where authorized users can create, edit, delete, and prioritize notices.

### 🎯 Aim
To replace manual/physical noticeboards with a **digital, priority-based board** that can run as a standalone `.exe` / `.jar` on any display computer.

---

## ✨ Features

- Separate **Admin** and **User** panels.  
- **Notice Priority** (High / Medium / Low) controls display order.  
- Create, edit, or delete notices.  
- Simple admin **authentication** (username + password).  
- **Auto-rotating** notice view for users.  
- Built-in **Hibernate ORM** for DB operations.  
- Packaged as **executable JAR / EXE** for easy deployment.

---

## 🧠 Technology Stack

| Layer | Technology |
|-------|-------------|
| **Language** | Java 8 + |
| **UI** | Java Swing |
| **ORM** | Hibernate |
| **Database** | MySQL |
| **Build Tool** | Apache Ant |
| **IDE** | NetBeans (Recommended) |
| **Packaging** | Runnable JAR / EXE |

---

## 🏗️ High-Level Architecture & Flow

+-----------------------------+
| SmartBoard |
+-------------+---------------+
|
+-----+-----+
| Admin UI |
+-----+-----+
|
[Hibernate ORM]
|
[MySQL DB]
|
+-----+-----+
| User UI |
+-----------+


### **Flow**
1. Application starts → initializes Hibernate & DB connection.  
2. **Admin Panel:** login → manage notices → save to DB.  
3. **User Panel:** fetch notices → display by priority + time.  
4. Admin updates reflect automatically in user display.

---

## 📂 Repository Structure

SmartBoard/
├── build.xml # Ant build script
├── manifest.mf # Jar manifest info
├── lib/ # Third-party libraries (Hibernate, MySQL Connector, etc.)
├── nbproject/ # NetBeans project metadata
├── src/
│ └── com/smartboard/
│ ├── ui/ # Swing forms and UI classes
│ ├── dao/ # DAO classes (CRUD via Hibernate)
│ ├── model/ # Entity classes (Notice, AdminUser)
│ ├── util/ # HibernateUtil, DB helpers
│ └── Main.java # Application entry point
└── README.md

## 🗄️ Database Schema

### **Table:** `notice`

```sql
CREATE TABLE notice (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  priority ENUM('HIGH','MEDIUM','LOW') DEFAULT 'LOW',
  image_path VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  publish_from DATETIME DEFAULT CURRENT_TIMESTAMP,
  publish_to DATETIME NULL,
  is_active BOOLEAN DEFAULT TRUE
);
Table: admin_user
sql
Copy code
CREATE TABLE admin_user (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
⚙️ Configuration
hibernate.cfg.xml
```
xml
```
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
          "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
          "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
  <session-factory>
    <property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
    <property name="connection.url">jdbc:mysql://localhost:3306/smartboard_db?useSSL=false&amp;serverTimezone=UTC</property>
    <property name="connection.username">your_db_user</property>
    <property name="connection.password">your_db_password</property>
    <property name="dialect">org.hibernate.dialect.MySQL8Dialect</property>
    <property name="show_sql">true</property>
    <property name="hbm2ddl.auto">update</property>

    <mapping class="com.smartboard.model.Notice"/>
    <mapping class="com.smartboard.model.AdminUser"/>
  </session-factory>
</hibernate-configuration>
```
---

🧰 Build & Run
Prerequisites
Java JDK 8 or later

MySQL Server running locally

NetBeans (Recommended) or Ant

Properly configured hibernate.cfg.xml

Steps
bash
Copy code
# Clone repository
git clone https://github.com/RuchitDoshi30/SmartBoard.git
cd SmartBoard

# Build (from NetBeans or Ant)
ant jar

# Run generated JAR
java -jar dist/SmartBoard.jar
To make a Windows EXE: use Launch4j or jpackage.

📦 JAR / Lib Files — Purpose & Explanation
JAR File	Purpose
mysql-connector-java-X.X.X.jar	JDBC driver for MySQL.
hibernate-core-X.X.X.Final.jar	Core Hibernate ORM library.
hibernate-commons-annotations.jar	Annotation support for Hibernate.
jboss-logging.jar / slf4j-api.jar	Logging APIs used by Hibernate.
antlr.jar / dom4j.jar / javassist.jar	Internal dependencies of Hibernate.
c3p0.jar (optional)	Connection pooling.
commons-logging.jar / log4j.jar	Logging implementations.

(Replace versions once you confirm exact lib filenames.)

🧩 How to Clone / Import to IDE
NetBeans
File → Open Project → SmartBoard

Add missing JARs via Project → Properties → Libraries → Add JAR/Folder

Run project (▶)

IntelliJ / Eclipse
Import as existing Java project.

Add lib/ jars to classpath.

Set Main.java as entry point.

💻 Usage — Admin & User Panels
User Panel
Displays active notices (auto-rotating / scrolling).

Read-only; intended for display screens.

Orders notices by priority and publish time.

Admin Panel
Login with valid credentials.

Create, edit, delete notices.

Set notice priority and schedule (optional).

Updates instantly reflected on User Panel.

🪛 Troubleshooting & Common Issues
Issue	Cause / Fix
DB connection error	Check MySQL service + credentials in hibernate.cfg.xml.
ClassNotFoundException	Ensure all required JARs exist in lib/ and classpath.
Hibernate mapping errors	Verify mapping package names and annotations.
Blank UI	Run Swing UI on Event Dispatch Thread (SwingUtilities.invokeLater).

🚀 Future Work / Improvements
Multi-admin support with role-based permissions.

Scheduled activation / expiry of notices.

Web dashboard for remote updates.

Auto-update feature for client machines.

Improved packaging using jpackage.

📜 License & Acknowledgements
License: MIT License
© 2025 Jaymin Dattani, Ruchit Doshi, Aditya Mevcha

Acknowledgements:
Special thanks to Professor Dr. Ravi Kumar Natarajan for guidance and subject instruction in the AJT course.

