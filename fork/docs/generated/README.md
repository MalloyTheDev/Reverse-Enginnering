# fork/docs/generated/ — Generated reports

This directory holds **generated** artifacts produced by tooling in this fork. It is
not hand-authored documentation.

## What's here

| File | Produced by | Description |
| --- | --- | --- |
| `EXTENSIONPOINT_INVENTORY.md` | `tools/extension-inventory.py` | Human-readable static inventory of likely Ghidra extension surfaces. |
| `extensionpoint_inventory.json` | `tools/extension-inventory.py --json` | Machine-readable form of the same inventory (full candidate lists). |

## How to regenerate

```bash
python3 tools/extension-inventory.py          # regenerate the Markdown report
python3 tools/extension-inventory.py --json    # also regenerate the JSON report
```

The scanner uses only the Python standard library, runs no Java/Gradle, compiles
nothing, and is read-only except for writing into this directory.

## Rules for this directory

- **These reports can be regenerated at any time.** Treat them as disposable output,
  not source of truth.
- **The static inventory is advisory, not authoritative.** It matches text patterns;
  it does not parse or compile Java. Every entry is a *candidate*. Confirm against the
  real source before relying on a specific result. See the warning block at the top of
  `EXTENSIONPOINT_INVENTORY.md`.
- **Do not manually edit generated reports**, except to add a short, clearly-marked
  note if you must annotate something. Any manual edit is lost on the next regenerate.
- **Regenerate after an upstream re-sync.** Counts and paths shift when Ghidra changes.
