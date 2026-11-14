// snipy website behaviour. Everything here enhances content that is already in the HTML.

(() => {
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const root = document.documentElement;
  addEventListener("load", () => root.classList.add("loaded"));
  setTimeout(() => root.classList.add("loaded"), 1200);

  // --- entrances ---------------------------------------------------------
  const reveals = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window && !reduced) {
    const io = new IntersectionObserver((entries) => {
      for (const e of entries) if (e.isIntersecting) { e.target.classList.add("in"); io.unobserve(e.target); }
    }, { rootMargin: "0px 0px -8% 0px" });
    reveals.forEach((el) => io.observe(el));
  } else reveals.forEach((el) => el.classList.add("in"));

  // --- the bar takes the ink of the chapter beneath it --------------------
  const bar = document.querySelector(".bar");
  const inks = { paper: "var(--ink-on-paper)", dark: "var(--ink)", surface: "var(--ink)", accent: "var(--accent-ink)" };
  const grounds = { paper: "var(--paper)", dark: "var(--bg)", surface: "var(--surface)", accent: "var(--accent)" };
  const chapters = [...document.querySelectorAll("[data-bar]")];
  const paintBar = () => {
    const y = bar.offsetHeight / 2;
    const under = chapters.find((c) => { const r = c.getBoundingClientRect(); return r.top <= y && r.bottom > y; });
    if (!under) return;
    bar.style.setProperty("--bar-ink", inks[under.dataset.bar]);
    bar.dataset.on = under.dataset.bar;
    // over the hero the bar stays transparent so the two fields run to the top
    bar.style.setProperty("--bar-bg", under.classList.contains("hero") ? "transparent" : grounds[under.dataset.bar]);
  };
  addEventListener("scroll", paintBar, { passive: true });
  addEventListener("resize", paintBar);
  paintBar();

  // --- write Scala, read Python ------------------------------------------
  const expr = document.getElementById("expr");
  const py = document.getElementById("py");
  const stepsEl = document.getElementById("steps");
  const modules = { math: "import math", json: "import json", np: "import numpy as np", os: "import os",
    random: "import random", statistics: "import statistics", re: "import re", itertools: "import itertools",
    functools: "import functools", datetime: "import datetime", fractions: "import fractions" };
  const collections = { "Map": ["{", "}", "dict"], "List": ["[", "]", "list"], "Seq": ["[", "]", "list"],
    "Vector": ["[", "]", "list"], "Set": ["{", "}", "set"], "Py.list": ["[", "]", "list"],
    "Py.tuple": ["(", ")", "tuple"], "Py": ["", "", null] };

  const matching = (s, open) => {
    let depth = 0, quote = null;
    for (let i = open; i < s.length; i++) {
      const c = s[i];
      if (quote) { if (c === "\\") i++; else if (c === quote) quote = null; continue; }
      if (c === '"' || c === "'") quote = c;
      else if (c === "(" || c === "[" || c === "{") depth++;
      else if (c === ")" || c === "]" || c === "}") { depth--; if (depth === 0) return i; }
    }
    return -1;
  };
  const splitTop = (s, sep) => {
    const parts = []; let depth = 0, quote = null, start = 0;
    for (let i = 0; i < s.length; i++) {
      const c = s[i];
      if (quote) { if (c === "\\") i++; else if (c === quote) quote = null; continue; }
      if (c === '"' || c === "'") quote = c;
      else if ("([{".includes(c)) depth++;
      else if (")]}".includes(c)) depth--;
      else if (depth === 0 && s.startsWith(sep, i)) { parts.push(s.slice(start, i)); start = i + sep.length; i += sep.length - 1; }
    }
    parts.push(s.slice(start));
    return parts.map((p) => p.trim()).filter((p, i, a) => p.length || a.length > 1);
  };
  const short = (s) => (s.length > 34 ? s.slice(0, 31) + "…" : s);
  const code = (s) => `<code>${s.replace(/&/g, "&amp;").replace(/</g, "&lt;")}</code>`;

  function translate(src, steps) {
    let out = "", i = 0;
    while (i < src.length) {
      const rest = src.slice(i);
      const prev = i > 0 ? src[i - 1] : " ";
      let m;
      if (!/[\w.]/.test(prev) && (m = rest.match(/^(Py\.tuple|Py\.list|Map|List|Seq|Vector|Set|Py)\(/))) {
        const open = i + m[1].length, close = matching(src, open);
        if (close < 0) throw new Error("unbalanced");
        const inner = src.slice(open + 1, close);
        const [l, r, kind] = collections[m[1]];
        let body;
        if (m[1] === "Map") body = splitTop(inner, ",").map((kv) => splitTop(kv, "->").map((x) => translate(x, steps)).join(": ")).join(", ");
        else body = splitTop(inner, ",").map((x) => translate(x, steps)).join(", ");
        if (kind) steps.push(`Converts ${code(short(src.slice(i, close + 1)))} to a <code>${kind}</code>`);
        out += l + body + (m[1] === "Py.tuple" && splitTop(inner, ",").length === 1 ? "," : "") + r;
        i = close + 1;
      } else if ((m = rest.match(/^\((\w+)(?::\s*[\w\[\], ]+)?\)\s*=>\s*/))) {
        const end = (() => { let d = 0; for (let j = i; j < src.length; j++) { if ("([{".includes(src[j])) d++; else if (")]}".includes(src[j])) { if (d === 0) return j; d--; } else if (src[j] === "," && d === 0) return j; } return src.length; })();
        const body = src.slice(i + m[0].length, end).replace(new RegExp(`\\b${m[1]}\\.(length|size)\\b`, "g"), `len(${m[1]})`).replace(new RegExp(`\\b${m[1]}\\.toLowerCase\\b`, "g"), `${m[1]}.lower()`);
        out += `lambda ${m[1]}: ${translate(body, [])}`;
        steps.push("Wraps the Scala lambda as a Python callable");
        i = end;
      } else if ((m = rest.match(/^\.as\[([^\]]*(?:\[[^\]]*\])?[^\]]*)\]/))) {
        steps.push(`Converts the result to <code>${m[1]}</code>`);
        i += m[0].length;
      } else if ((m = rest.match(/^\.matmul\(/))) {
        const open = i + m[0].length - 1, close = matching(src, open);
        if (close < 0) throw new Error("unbalanced");
        out += " @ " + translate(src.slice(open + 1, close), steps);
        i = close + 1;
      } else if ((m = rest.match(/^\.floorDiv\(/))) {
        const open = i + m[0].length - 1, close = matching(src, open);
        if (close < 0) throw new Error("unbalanced");
        out += " // " + translate(src.slice(open + 1, close), steps);
        i = close + 1;
      } else if ((m = rest.match(/^(Python\.)?builtins\./))) {
        i += m[0].length;
      } else if ((m = rest.match(/^Python\.(eval|module)\(("[^"]*")\)/))) {
        out += m[1] === "eval" ? JSON.parse(m[2]) : JSON.parse(m[2]);
        i += m[0].length;
      } else if ((m = rest.match(/^module\(("[^"]*")\)/))) {
        out += JSON.parse(m[1]);
        i += m[0].length;
      } else if ((m = rest.match(/^(\w+)\s=(?!=)\s*/)) && /[(,]\s*$/.test(out)) {
        steps.push(`Passes <code>${m[1]}=</code> as a keyword argument`);
        out += m[1] + "=";
        i += m[0].length;
      } else if ((m = rest.match(/^\b(true|false)\b/))) {
        out += m[1] === "true" ? "True" : "False";
        i += m[0].length;
      } else if (rest.startsWith("**")) {
        out += " ** "; i += 2;
        while (src[i] === " ") i++;
        out = out.replace(/\s+\*\* $/, " ** ");
      } else if (rest[0] === '"') {
        const end = src.indexOf('"', i + 1);
        if (end < 0) throw new Error("open string");
        out += src.slice(i, end + 1); i = end + 1;
      } else { out += src[i]; i++; }
    }
    return out;
  }

  function render() {
    const src = expr.value.trim();
    if (!src) { py.textContent = ""; stepsEl.innerHTML = "<li>Type a snipy expression</li>"; return; }
    try {
      const steps = [];
      let body = translate(src, steps).replace(/\s+/g, " ").replace(/\(\s+/g, "(").replace(/\s+\)/g, ")").trim();
      body = body.replace(/^\((.*)\)$/, (all, inner) => (matching(all, 0) === all.length - 1 ? inner : all));
      const used = new Set([...src.matchAll(/(?<![\w.])(\w+)\./g)].map((m) => m[1]));
      const imports = Object.keys(modules).filter((k) => used.has(k)).map((k) => modules[k]);
      // every call and operator the expression makes Python run
      const scan = body.replace(/"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'/g, '""').replace(/lambda \w+:[^,)]*/g, "");
      const calls = [...scan.matchAll(/(\.?[A-Za-z_][\w.]*)\(/g)].map((m) => m[1]);
      const operators = [...scan.matchAll(/\s(@|\*\*|\/\/)\s/g)].map((m) => m[1]);
      if (calls.length === 1) steps.push(`Calls ${code(calls[0])} with one vectorcall`);
      else if (calls.length > 1) steps.push(`Makes ${calls.length} vectorcalls: ${calls.map(code).join(", ")}`);
      for (const op of new Set(operators)) steps.push(`Applies ${code(op)} through Python's number protocol`);
      // the conversion back to Scala happens last
      const last = steps.filter((x) => x.startsWith("Converts the result"));
      steps.splice(0, steps.length, ...steps.filter((x) => !last.includes(x)), ...last);
      py.textContent = [...imports, body].join("\n");
      stepsEl.innerHTML = [...new Set(steps)].slice(0, 6).map((s) => `<li>${s}</li>`).join("") || "<li>Evaluates the expression in Python</li>";
    } catch {
      stepsEl.innerHTML = "<li>Keep typing: the expression isn't complete yet</li>";
    }
  }
  // shrink the line to fit its width, down to a floor; past that it scrolls
  const fit = () => {
    expr.style.fontSize = "";
    const max = parseFloat(getComputedStyle(expr).fontSize);
    const floor = Math.min(max, 22);
    const ratio = expr.clientWidth / expr.scrollWidth;
    if (ratio < 1) expr.style.fontSize = Math.max(floor, Math.floor(max * ratio * 0.98)) + "px";
  };
  expr.addEventListener("input", fit);
  addEventListener("resize", fit);
  document.fonts?.ready.then(fit);
  expr.addEventListener("input", render);
  document.querySelectorAll("[data-expr]").forEach((b) =>
    b.addEventListener("click", () => { expr.value = b.dataset.expr; render(); fit(); expr.focus(); }));
  render();
  fit();

  // --- capabilities as a command menu -------------------------------------
  const find = document.getElementById("find");
  const items = [...document.querySelectorAll("#caplist li")];
  const none = document.getElementById("none");
  const select = (li) => items.forEach((x) => x.setAttribute("aria-selected", String(x === li)));
  items.forEach((li) => {
    const b = li.querySelector("button");
    b.addEventListener("click", () => select(li));
    b.addEventListener("mouseenter", () => { if (matchMedia("(hover: hover)").matches) select(li); });
    b.addEventListener("keydown", (e) => {
      const visible = items.filter((x) => !x.hidden);
      const at = visible.indexOf(li);
      const to = e.key === "ArrowDown" ? visible[at + 1] : e.key === "ArrowUp" ? visible[at - 1] : null;
      if (to) { e.preventDefault(); select(to); to.querySelector("button").focus(); }
    });
  });
  find.addEventListener("input", () => {
    const q = find.value.trim().toLowerCase();
    let first = null;
    items.forEach((li) => {
      const hit = !q || li.textContent.toLowerCase().includes(q);
      li.hidden = !hit;
      if (hit && !first) first = li;
    });
    none.hidden = !!first;
    if (first && items.find((x) => x.getAttribute("aria-selected") === "true")?.hidden !== false) select(first);
  });
  find.addEventListener("keydown", (e) => {
    if (e.key === "ArrowDown") { const f = items.find((x) => !x.hidden); if (f) { e.preventDefault(); select(f); f.querySelector("button").focus(); } }
  });

  // --- the tested-with sentence, one name lit at a time --------------------
  const names = [...document.querySelectorAll("#tested b")];
  let lit = 0;
  names[0]?.classList.add("lit");
  if (!reduced && names.length) setInterval(() => {
    names[lit].classList.remove("lit");
    lit = (lit + 1) % names.length;
    names[lit].classList.add("lit");
  }, 2000);
})();
