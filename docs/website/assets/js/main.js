/* RedisFront 官网交互：主题、语言、截图画廊 */
(function () {
  "use strict";

  var STORAGE_KEY = "redisfront.site";

  function readState() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
    } catch (e) {
      return {};
    }
  }

  function writeState(state) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (e) { /* 隐私模式等场景下静默失败 */ }
  }

  var state = readState();

  /* ---------- 主题：手动选择优先，否则跟随系统 ---------- */
  function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    var btn = document.getElementById("theme-toggle");
    if (btn) {
      btn.textContent = theme === "dark" ? "☀" : "🌙";
      btn.setAttribute("aria-label", theme === "dark" ? "Switch to light" : "Switch to dark");
    }
  }

  var systemDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)");
  applyTheme(state.theme || (systemDark && systemDark.matches ? "dark" : "light"));

  if (systemDark) {
    // 未手动选择时跟随系统变化
    systemDark.addEventListener("change", function (e) {
      if (!state.theme) { applyTheme(e.matches ? "dark" : "light"); }
    });
  }

  var themeBtn = document.getElementById("theme-toggle");
  if (themeBtn) {
    themeBtn.addEventListener("click", function () {
      var next = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
      state.theme = next;
      writeState(state);
      applyTheme(next);
    });
  }

  /* ---------- 语言：默认中文，持久化到 localStorage ---------- */
  function applyLang(lang) {
    var html = document.documentElement;
    html.classList.remove("zh", "en");
    html.classList.add(lang);
    html.setAttribute("lang", lang === "zh" ? "zh-CN" : "en");
    var btn = document.getElementById("lang-toggle");
    if (btn) {
      btn.textContent = lang === "zh" ? "EN" : "中";
      btn.setAttribute("aria-label", lang === "zh" ? "Switch to English" : "切换到中文");
    }
  }

  var lang = state.lang || ((navigator.language || "").toLowerCase().indexOf("zh") === 0 ? "zh" : "en");
  applyLang(lang);

  var langBtn = document.getElementById("lang-toggle");
  if (langBtn) {
    langBtn.addEventListener("click", function () {
      lang = lang === "zh" ? "en" : "zh";
      state.lang = lang;
      writeState(state);
      applyLang(lang);
    });
  }

  /* ---------- Hero 截图轮播：每 5 秒交叉淡入下一张 ---------- */
  var carousel = document.querySelector(".carousel");
  if (carousel) {
    var frames = carousel.querySelectorAll("img");
    var frameIndex = 0;
    setInterval(function () {
      frames[frameIndex].classList.remove("active");
      frameIndex = (frameIndex + 1) % frames.length;
      frames[frameIndex].classList.add("active");
    }, 5000);
  }

  /* ---------- 年份 ---------- */
  var year = document.getElementById("year");
  if (year) { year.textContent = new Date().getFullYear(); }
})();
