# Camunda Model Summary

Process ID: `hostel-outpass`

Process name: `Hostel Out-Pass Request`

Service task types:
- `check-outpass`
- `issue-outpass`

User task:
- `Warden approval`
- Form ID: `warden-approval-form`

Gateway conditions:
- Invalid: `=not(valid)`
- Day out: `=valid and nights = 0`
- Overnight: `=valid and nights >= 1`
