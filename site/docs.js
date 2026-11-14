// Renders docs/*.md, the same files GitHub shows, into the docs page.
(() => {
  const pages = {
    "getting-started": "docs/getting-started.md",
    guide: "docs/guide.md",
    memory: "docs/memory.md",
    migration: "docs/migration.md",
    changelog: "CHANGELOG.md",
  };
  const doc = document.getElementById("doc");
  const outline = document.getElementById("outline");
  const slug = (s) => s.toLowerCase().replace(/[`'".,:()/]/g, "").trim().replace(/\s+/g, "-");

  // links between the markdown files become links between pages here
  const rewrite = (href) => {
    const m = href.match(/^(?:docs\/)?([\w-]+)\.md(#.*)?$/);
    if (m && (m[1] in pages || m[1] === "CHANGELOG")) return "#" + (m[1] === "CHANGELOG" ? "changelog" : m[1]);
    if (href === "LICENSE") return "https://github.com/lolgab/snipy/blob/master/LICENSE";
    return href;
  };

  async function show() {
    const [page, anchor] = (location.hash.slice(1) || "getting-started").split(":");
    const file = pages[page] || pages["getting-started"];
    const current = "#" + (pages[page] ? page : "getting-started");
    document.querySelectorAll(".toc ul a").forEach((a) => {
      if (a.getAttribute("href") === current) a.setAttribute("aria-current", "page");
      else a.removeAttribute("aria-current");
    });
    try {
      const res = await fetch(file);
      if (!res.ok) throw new Error(res.status);
      doc.innerHTML = marked.parse(await res.text());
    } catch {
      doc.innerHTML = `<h1>Could not load this page</h1><p>Read it on <a href="https://github.com/lolgab/snipy/blob/master/${file}">GitHub</a>.</p>`;
      return;
    }
    doc.querySelectorAll("a[href]").forEach((a) => a.setAttribute("href", rewrite(a.getAttribute("href"))));
    doc.querySelectorAll("pre code").forEach((c) => {
      if (!c.className) c.classList.add("language-plaintext");
      hljs.highlightElement(c);
    });
    outline.innerHTML = "";
    doc.querySelectorAll("h2").forEach((h) => {
      h.id = slug(h.textContent);
      const li = document.createElement("li");
      li.innerHTML = `<a href="#${page}:${h.id}">${h.textContent}</a>`;
      outline.append(li);
    });
    document.title = `${doc.querySelector("h1")?.textContent || "Docs"} · snipy`;
    if (anchor) document.getElementById(anchor)?.scrollIntoView();
    else { scrollTo(0, 0); doc.focus({ preventScroll: true }); }
  }
  addEventListener("hashchange", show);
  show();
})();
