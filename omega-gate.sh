#!/usr/bin/env sh
set -eu

platform="${1:-auto}"

if [ "$platform" = "auto" ]; then
  system="$(uname -s 2>/dev/null || echo unknown)"
  case "$system" in
    Darwin) platform="macos" ;;
    Linux) platform="linux" ;;
    MINGW*|MSYS*|CYGWIN*) platform="windows" ;;
    *) platform="unknown" ;;
  esac
fi

case "$platform" in
  android) door="android"; normalized="omega_a" ;;
  apple) door="apple"; normalized="omega_al" ;;
  windows) door="desktop"; normalized="omega_w" ;;
  macos) door="desktop"; normalized="omega_m" ;;
  linux) door="desktop"; normalized="omega_l" ;;
  *) echo "Unsupported platform: $platform" >&2; exit 1 ;;
esac

echo "JULE Omega Gate"
echo "Platform: $platform"
echo "Normalized as: $normalized"
echo "Omega origin: (0,0,0)"
echo "Selected door: $door"
echo "Source path: $(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/$door"
