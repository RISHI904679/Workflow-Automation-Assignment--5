# Hostel Out-Pass Workflow — Camunda 8 SaaS + Spring Boot

This project implements the **Hostel Out-Pass Request** assignment from the Student Leave Workflow Lab.

## Process rules

1. `POST /outpass` accepts `studentName`, `regNo`, `hostelBlock`, `outDate`, `returnDate`, `parentPhone`, and `purpose`.
2. Worker `check-outpass` calculates `nights = DAYS.between(outDate, returnDate)` and sets `valid = false` when the return date is before the out date.
3. The gateway routes:
   - `not(valid)` → reject path.
   - `valid and nights = 0` → automatic approval.
   - `valid and nights >= 1` → Warden approval in Tasklist.
4. Worker `issue-outpass` creates `OP-<regNo>-<outDate>` when approved and logs an SMS-to-parent message.
5. The process ends with `passStatus = ISSUED` or `REJECTED`.

## Project structure

```text
hostel-outpass-workflow/
├── pom.xml
├── README.md
├── .gitignore
├── docs/
│   ├── variable-note.md
│   └── test-cases.md
└── src/main/
    ├── java/edu/srmist/outpass/
    │   ├── OutpassWorkflowApplication.java
    │   ├── api/OutpassController.java
    │   └── worker/OutpassWorkers.java
    └── resources/
        ├── application.yaml
        ├── application-example.yaml
        └── bpmn/
            ├── hostel-outpass.bpmn
            └── warden-approval.form
```

## 1. Create the Camunda SaaS client

Create a Camunda 8 SaaS cluster using the 8.8 generation. Create an API client with the **Zeebe / Orchestration Cluster** scope.

Keep the generated Client ID, Client Secret, Cluster ID, and Region private. Do not commit the secret.

## 2. Configure credentials

Recommended: set environment variables before starting the application.

Windows PowerShell:

```powershell
$env:CAMUNDA_CLIENT_ID="YOUR_CLIENT_ID"
$env:CAMUNDA_CLIENT_SECRET="YOUR_CLIENT_SECRET"
$env:CAMUNDA_CLUSTER_ID="YOUR_CLUSTER_ID"
$env:CAMUNDA_CLUSTER_REGION="YOUR_CLUSTER_REGION"
```

macOS/Linux:

```bash
export CAMUNDA_CLIENT_ID="YOUR_CLIENT_ID"
export CAMUNDA_CLIENT_SECRET="YOUR_CLIENT_SECRET"
export CAMUNDA_CLUSTER_ID="YOUR_CLUSTER_ID"
export CAMUNDA_CLUSTER_REGION="YOUR_CLUSTER_REGION"
```

The application also contains `application-example.yaml` to show the required values.

## 3. Build and run

Requirements: JDK 17+ and Maven 3.9+.

```bash
mvn clean package
mvn spring-boot:run
```

At startup, the Spring Boot application deploys both:

- `hostel-outpass.bpmn`
- `warden-approval.form`

It also registers the two workers:

- `check-outpass`
- `issue-outpass`

## 4. Test the REST endpoint

### Case A — day out, auto-approved

```bash
curl -X POST http://localhost:8080/outpass ^
  -H "Content-Type: application/json" ^
  -d "{\"studentName\":\"Priya\",\"regNo\":\"RA2311003010001\",\"hostelBlock\":\"N\",\"outDate\":\"2026-10-25\",\"returnDate\":\"2026-10-25\",\"parentPhone\":\"9876543210\",\"purpose\":\"Medical appointment\"}"
```

For macOS/Linux, use the same JSON with normal single quotes around it:

```bash
curl -X POST http://localhost:8080/outpass \
  -H 'Content-Type: application/json' \
  -d '{"studentName":"Priya","regNo":"RA2311003010001","hostelBlock":"N","outDate":"2026-10-25","returnDate":"2026-10-25","parentPhone":"9876543210","purpose":"Medical appointment"}'
```

Expected final variable: `passStatus = ISSUED` and a pass number such as `OP-RA2311003010001-2026-10-25`.

### Case B — overnight, requires Warden approval

```json
{
  "studentName": "Priya",
  "regNo": "RA2311003010001",
  "hostelBlock": "N",
  "outDate": "2026-10-25",
  "returnDate": "2026-10-27",
  "parentPhone": "9876543210",
  "purpose": "Family function"
}
```

The process waits at **Warden approval** in Tasklist. Assign the task to yourself, tick **Approve this out-pass**, enter a remark, and complete the task.

Expected final variable: `passStatus = ISSUED` and a generated `passNumber`.

To test rejection, submit the same overnight request but leave the approval checkbox unticked and enter a rejection remark. Expected final variable: `passStatus = REJECTED`.

### Case C — invalid dates

Use a return date before the out date, for example:

```json
{
  "studentName": "Priya",
  "regNo": "RA2311003010001",
  "hostelBlock": "N",
  "outDate": "2026-10-27",
  "returnDate": "2026-10-25",
  "parentPhone": "9876543210",
  "purpose": "Invalid-date test"
}
```

Expected final variable: `passStatus = REJECTED`, with no Warden task created.

## 5. Screenshots required for submission

Capture these from your own Camunda SaaS account after running the application:

1. Operate — auto-approved day-out instance, showing the completed path and variables.
2. Operate — Warden-approved overnight instance, showing the completed Warden path and variables.
3. Operate — rejected invalid-date instance, showing the invalid path and variables.
4. Tasklist — Warden approval form before completion.

Do not use fabricated screenshots; the screenshots must show your own cluster/process instances.

## 6. Git commands

```bash
git init
git add .
git commit -m "Implement hostel out-pass Camunda workflow"
git branch -M main
git remote add origin YOUR_GITHUB_REPOSITORY_URL
git push -u origin main
```

Never commit your Camunda client secret. The repository is intentionally configured to read credentials from environment variables.
