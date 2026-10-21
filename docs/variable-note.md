# Variable Note — Hostel Out-Pass Workflow

The workflow uses process variables passed from the REST request, variables produced by the check worker, and variables entered by the Warden through Tasklist.

| Worker / stage | Reads | Writes |
|---|---|---|
| `POST /outpass` | Student request JSON | `studentName`, `regNo`, `hostelBlock`, `outDate`, `returnDate`, `parentPhone`, `purpose` |
| `check-outpass` | `regNo`, `outDate`, `returnDate` | `nights`, `valid`, `approved` (`true` for a day-out request) |
| Warden form | `studentName`, `regNo`, `hostelBlock`, `outDate`, `returnDate`, `parentPhone`, `purpose` | `approved`, `wardenRemarks` |
| `issue-outpass` | `regNo`, `outDate`, `studentName`, `parentPhone`, `approved`, `wardenRemarks` | `passStatus`, `passNumber` |

## Routing logic

- `valid = false` → rejection path → `issue-outpass` → `passStatus = REJECTED`.
- `valid = true` and `nights = 0` → auto-approved → `issue-outpass` → `passStatus = ISSUED`.
- `valid = true` and `nights >= 1` → Warden approval → `issue-outpass` → `ISSUED` or `REJECTED` depending on `approved`.
