# Test Cases and Expected Results

| Case | Input | Expected route | Expected result |
|---|---|---|---|
| Auto-approved | outDate = returnDate | Check request → Day out → Issue out-pass | `passStatus=ISSUED`, `passNumber=OP-<regNo>-<outDate>` |
| Warden approved | returnDate after outDate; Warden checks approval | Check request → Overnight → Warden approval → Issue out-pass | `passStatus=ISSUED` with pass number |
| Warden rejected | returnDate after outDate; Warden leaves approval unchecked | Check request → Overnight → Warden approval → Issue out-pass | `passStatus=REJECTED` |
| Invalid dates | returnDate before outDate | Check request → Invalid → Issue out-pass | `passStatus=REJECTED`, no Warden task |
