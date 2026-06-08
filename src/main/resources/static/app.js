const sampleScenario = {
  title: "Book addition",
  externalActors: ["Librarian"],
  systemActors: ["System"],
  rootStep: {
    subSteps: [
      { text: "Librarian selects option to add a new book" },
      { text: "System displays a form" },
      {
        text: "IF: Librarian wants to add copies",
        subSteps: [
          { text: "Librarian defines copy instances" },
          {
            text: "FOR EACH: copy instance",
            subSteps: [
              { text: "System prompts for copy details" },
              { text: "Librarian confirms copy details" }
            ]
          }
        ]
      },
      { text: "System confirms book addition" }
    ]
  }
};

const optionIds = [
  "includeTotalStepCount",
  "includeKeywordStepCount",
  "includeStepsWithoutActors",
  "includeNumberedScenario",
  "includeLimitedScenario",
  "includeInvalidSteps"
];

const scenarioInput = document.getElementById("scenarioInput");
const statusBadge = document.getElementById("statusBadge");
const httpStatus = document.getElementById("httpStatus");
const analyzeButton = document.getElementById("analyzeButton");
const textButton = document.getElementById("textButton");

document.getElementById("sampleButton").addEventListener("click", loadSample);
document.getElementById("clearButton").addEventListener("click", clearInput);
analyzeButton.addEventListener("click", analyzeScenario);
textButton.addEventListener("click", downloadText);

document.querySelectorAll(".tab").forEach((button) => {
  button.addEventListener("click", () => setActiveTab(button.dataset.tab));
});

loadSample();
renderEmptyState();

function loadSample() {
  scenarioInput.value = JSON.stringify(sampleScenario, null, 2);
}

function clearInput() {
  scenarioInput.value = "";
  renderEmptyState();
}

function readPayload() {
  const parsed = JSON.parse(scenarioInput.value);
  const scenario = parsed.scenario && parsed.options ? parsed.scenario : parsed;

  return {
    scenario,
    options: readOptions()
  };
}

function readOptions() {
  const options = {};
  optionIds.forEach((id) => {
    options[id] = document.getElementById(id).checked;
  });
  options.maxDepth = Number.parseInt(document.getElementById("maxDepth").value, 10) || 0;
  return options;
}

async function analyzeScenario() {
  setBusy(true);
  try {
    const response = await fetch("/api/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(readPayload())
    });
    const body = await response.json();
    renderResponse(response.status, body);
  } catch (error) {
    renderError(error);
  } finally {
    setBusy(false);
  }
}

async function downloadText() {
  setBusy(true);
  try {
    const response = await fetch("/api/analyze/text", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(readPayload())
    });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(text || `HTTP ${response.status}`);
    }

    const url = URL.createObjectURL(new Blob([text], { type: "text/plain" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = "scenario.txt";
    link.click();
    URL.revokeObjectURL(url);
    setStatus("Downloaded", "ok");
  } catch (error) {
    renderError(error);
  } finally {
    setBusy(false);
  }
}

function renderResponse(status, body) {
  httpStatus.textContent = `HTTP ${status}`;
  setStatus(body.status || "Done", status >= 200 && status < 300 ? "ok" : "error");

  document.getElementById("totalStepCount").textContent = valueOrDash(body.totalStepCount);
  document.getElementById("keywordStepCount").textContent = valueOrDash(body.keywordStepCount);
  document.getElementById("missingActorCount").textContent = Array.isArray(body.stepsWithoutActors)
    ? body.stepsWithoutActors.length
    : 0;

  renderList("warningsList", body.warnings);
  renderList("stepsWithoutActorsList", body.stepsWithoutActors);
  document.getElementById("textualScenario").textContent = Array.isArray(body.textualScenario)
    ? body.textualScenario.join("\n")
    : "";
  document.getElementById("jsonResult").textContent = JSON.stringify(body, null, 2);
}

function renderList(id, items) {
  const list = document.getElementById(id);
  list.replaceChildren();

  if (!items || items.length === 0) {
    const empty = document.createElement("li");
    empty.textContent = "-";
    list.appendChild(empty);
    return;
  }

  items.forEach((item) => {
    const li = document.createElement("li");
    li.textContent = item;
    list.appendChild(li);
  });
}

function renderError(error) {
  httpStatus.textContent = "Request failed";
  setStatus("Error", "error");
  document.getElementById("jsonResult").textContent = error.message;
  setActiveTab("json");
}

function renderEmptyState() {
  httpStatus.textContent = "No request";
  setStatus("Ready");
  document.getElementById("totalStepCount").textContent = "-";
  document.getElementById("keywordStepCount").textContent = "-";
  document.getElementById("missingActorCount").textContent = "-";
  renderList("warningsList", null);
  renderList("stepsWithoutActorsList", null);
  document.getElementById("textualScenario").textContent = "";
  document.getElementById("jsonResult").textContent = "";
}

function setActiveTab(tabName) {
  document.querySelectorAll(".tab").forEach((button) => {
    button.classList.toggle("active", button.dataset.tab === tabName);
  });
  document.querySelectorAll(".tab-panel").forEach((panel) => {
    panel.classList.add("hidden");
  });
  document.getElementById(`${tabName}Tab`).classList.remove("hidden");
}

function setBusy(isBusy) {
  analyzeButton.disabled = isBusy;
  textButton.disabled = isBusy;
}

function setStatus(text, mode = "") {
  statusBadge.textContent = text;
  statusBadge.classList.toggle("ok", mode === "ok");
  statusBadge.classList.toggle("error", mode === "error");
}

function valueOrDash(value) {
  return value === null || value === undefined ? "-" : value;
}
