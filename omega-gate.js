/**
 * JULE public Omega Gate
 *
 * Root-level public dispatcher contract.
 * This file exposes platform routing only. It does not disclose the
 * proprietary JULE normalization implementation.
 */
(function (global) {
  "use strict";

  const OMEGA_0 = Object.freeze({ x: 0, y: 0, z: 0 });

  function detectPlatform(environment) {
    const env = environment || {};
    const ua = String(env.userAgent || "").toLowerCase();
    const platform = String(env.platform || "").toLowerCase();
    const touchPoints = Number(env.maxTouchPoints || 0);

    if (ua.includes("android")) return "android";
    if (
      ua.includes("iphone") ||
      ua.includes("ipad") ||
      ua.includes("ipod") ||
      (platform === "macintel" && touchPoints > 1)
    ) return "apple";
    if (ua.includes("windows") || platform.includes("win")) return "windows";
    if (ua.includes("macintosh") || platform.includes("mac")) return "macos";
    if (ua.includes("linux") || platform.includes("linux")) return "linux";

    return "unknown";
  }

  function selectDoor(platform) {
    switch (platform) {
      case "android":
        return { platform, normalizedAs: "omega_a", path: "android", omega0: OMEGA_0 };
      case "apple":
        return { platform, normalizedAs: "omega_al", path: "apple", omega0: OMEGA_0 };
      case "windows":
        return { platform, normalizedAs: "omega_w", path: "desktop", omega0: OMEGA_0 };
      case "macos":
        return { platform, normalizedAs: "omega_m", path: "desktop", omega0: OMEGA_0 };
      case "linux":
        return { platform, normalizedAs: "omega_l", path: "desktop", omega0: OMEGA_0 };
      default:
        return { platform: "unknown", normalizedAs: "omega_u", path: null, omega0: OMEGA_0 };
    }
  }

  function enter(environment) {
    return selectDoor(detectPlatform(environment));
  }

  const api = Object.freeze({
    OMEGA_0,
    detectPlatform,
    selectDoor,
    enter
  });

  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  }
  global.JuleOmegaGate = api;
})(typeof globalThis !== "undefined" ? globalThis : this);
