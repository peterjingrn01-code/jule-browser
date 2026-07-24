# JULE™ Browser — Root Omega Gate Structure

JULE means **JSL Universal Language Execution**. Browser is its first public function.

## Root structure

```text
jule-browser/
├── omega-gate.js
├── omega-gate.json
├── omega-gate.ps1
├── omega-gate.sh
├── android/
├── apple/
├── desktop/
├── README.md
└── LICENSE.txt
```

The Omega Gate is at the repository root, parallel with the platform folders.

It detects the platform and selects one of three public branches:

- `android/`
- `apple/`
- `desktop/` for Windows, macOS, and Linux

The public gate contains only platform detection and routing. The proprietary
JULE normalization engine, runtime, JSCS mapping, JSEIS, and protected structural
execution implementation are not included.

## Important

This is a unified source tree, not one identical installer file. Each operating
system still generates its required native package:

- Android → APK
- Apple → signed app / IPA / TestFlight
- Windows → EXE
- macOS → APP / DMG
- Linux → AppImage / DEB
