Refine.ManageExtensionsUI = function(elmt) {
    elmt.html(DOM.loadHTML("core", "scripts/index/manage-extensions-ui.html"));

    this._elmt = elmt;
    this._elmts = DOM.bind(elmt);

    document.querySelector("#openExtensionDirectory").addEventListener("click", Refine.ManageExtensionsUI._openExtensionDirectory);

    Refine.ManageExtensionsUI._renderExtensions();
};

Refine.ManageExtensionsUI._fetchExtensions = function() {
    return fetch("/command/core/get-version").then(response => response.json()).then(data => {
        return data.module_names;
    });
}

Refine.ManageExtensionsUI._openExtensionDirectory = function() {
    fetch("/command/core/get-csrf-token").then(response => response.json()).then(data => {
        fetch("/command/core/open-extensions-dir", { method: "POST", body: new URLSearchParams({ csrf_token: data.token }) }).catch(error => {
            console.error("Failed to open extension directory", error);
        });
    });
}

Refine.ManageExtensionsUI._renderExtensions = function() {
    const coreExtensions = ["core", "database", "gdata", "jython", "pc-axis", "wikidata"]; // TODO: manage this on the server

    Refine.ManageExtensionsUI._fetchExtensions().then(extensions => {
        const extensionContainer = document.querySelector("tbody#extensionList");
        extensions.forEach(extension => {
            const extensionRow = document.createElement("tr");
            extensionRow.innerHTML = `
                <td>${extension}</td>
                <td>${coreExtensions.includes(extension) ? "true" : "false"}</td>
            `;
            extensionContainer.appendChild(extensionRow);
        });
    });
}

Refine.actionAreas.push({
    id: "manage-extensions",
    label: "Manage extensions", // TODO: i18n
    uiClass: Refine.ManageExtensionsUI,
});
