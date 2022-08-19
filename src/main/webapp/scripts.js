function matchFilter(item, municipalityMap, filter) {
    let filterLower = filter.toLowerCase();

    return item.location.toLowerCase().includes(filterLower) ||
        item.municipality_id.toLowerCase().includes(filterLower) ||
        municipalityMap.get(item.municipality_id).name.toLowerCase().includes(filterLower);
}

function loadTable(municipalities, filter) {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/tomyappointment/rest/allthemes");
    xhr.send();

    let municipalityMap = new Map();
    municipalities.forEach(mun => municipalityMap.set(mun.id, mun));

    const table = document.getElementById("tableBody");

    // Reset table.
    table.innerHTML = "";

    const loadingText = document.getElementById("loading");

    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            let response = xhr.response;

            console.log(response);

            const items = JSON.parse(response);

            if (items.length === 0) {
                let row = table.insertRow();
                let name = row.insertCell(0);
                name.innerHTML = "No themes created yet."
            }

            Array.from(items).filter(item => matchFilter(item, municipalityMap, filter)).forEach(item => {
                let row = table.insertRow();
                let id = row.insertCell(0);
                id.innerHTML = item.municipality_id;

                let muniName = municipalityMap.get(item.municipality_id).name || "Could not match to a municipality id!";

                let name = row.insertCell(1);
                name.innerHTML = muniName;
                let location = row.insertCell(2);
                location.innerHTML = item.location;
                let options = row.insertCell(3);

                options.innerHTML += `
                <button onclick='readyToEdit("${item.municipality_id}", "${item.id}")' type="button" class="btn" style="display: inline; float:left; width:30%; margin: 2px;" data-bs-popper='edit-modal' data-bs-target='#edit'>
                    Edit
                </button>`;
                options.innerHTML += `
                <button type="button" class="btn" style="display: inline; float:left; width:30%; margin: 2px;" data-bs-popper='display-modal' onclick='exportTheme("${item.municipality_id}", "${item.id}")'>
                    Export
                </button>`;
                options.innerHTML += `
                <button onclick='readyToDelete("${item.municipality_id}", "${item.id}", this.parentNode.parentNode)' type="button" class="btn" style="display: inline; float:left; width:25%; margin: 2px;" data-bs-popper='delete-modal' data-bs-target='#delete'>
                    Delete
                </button>`;
            });

            loadingText.style.display = "none";

        } else if (this.status === 404) {
            loadingText.innerHTML = "Failed to load themes."
        }
    }
}

let createModalDoc = document.getElementById('createMuniModal');
let createModal = new bootstrap.Modal(createModalDoc);

function createMunicipality() {
    let muni_id = document.getElementById("muni_id");
    let name = document.getElementById("muni_name");
    let short_name = document.getElementById("muni_short_name");
    // call the REST api to create a new municipality in the database.

    if (muni_id.value.length !== 7) {
        showNotification("Error", "Municipality ID should have a length of 7.");
        return;
    }

    if (name.value.length === 0) {
        showNotification("Error", "Please enter a name for the municipality.");
        return;
    }

    if (short_name.value.length === 0) {
        showNotification("Error", "Please enter a short name for the municipality.");
        return;
    }


    let formData = new FormData();
    formData.append("id", muni_id?.value);
    formData.append("name", name?.value);
    formData.append("short_name", short_name?.value);

    let xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomyappointment/rest/municipalities");

    xhr.send(formData);

    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {

            updateDropDown(false);

            document.getElementById("muni_name").value = "";
            document.getElementById("muni_short_name").value = "";

            showNotification("Success", "Succesfully created municipality.")

            createModal.hide();
        }
    }
}

function createTheme() {
    document.getElementById('createThemeModalLabel').innerHTML = "Create a new theme";
    document.getElementById('save_theme').innerHTML = "Create";
    document.getElementById("theme_guid").disabled = false;
    document.getElementById("save_theme").onclick = function () {
        createTheme();
    }

    if (!doValidation("Cannot create theme: ", false)) {
        return;
    }
    let muni_id = document.getElementById("muni-dropdown")?.value;

    let location = document.getElementById("location_name");

    let home_url = document.getElementById("home_url");

    let theme_id = document.getElementById("theme_guid")?.value;

    let background_image = document.getElementById("bg_image").files[0];

    let logo_image = document.getElementById("logo_image").files[0];

    let icon_image192 = document.getElementById("icon_192").files[0];

    let icon_image512 = document.getElementById("icon_512").files[0];

    let colour = document.getElementById("main_color");

    let font_family = document.getElementById("font_family")?.value;

    let radios = document.getElementsByName('font-setting');

    let formData = new FormData();
    formData.append("id", theme_id);
    formData.append("location", location?.value);
    formData.append("colour", colour?.value);
    formData.append("home_url", home_url?.value);

    for (let i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            if (radios[i].value === "1") {
                let fontfile = document.getElementById("font_file").files[0];

                formData.append("font", fontfile);
                formData.append("font_family", font_family);

            } else if (radios[i].value === "2") {
                let fontlink = document.getElementById("font_link")?.value;

                formData.append("font_url", fontlink);
                formData.append("font_family", font_family);
            }
            break;
        }
    }

    formData.append("background", background_image);
    formData.append("logo", logo_image);
    formData.append("icon-192", icon_image192);
    formData.append("icon-512", icon_image512);

    let xhr = new XMLHttpRequest();
    xhr.open("POST", "/tomyappointment/rest/municipalities/" + muni_id + "/themes");

    xhr.send(formData);

    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {

            $('#createThemeModal').modal('hide');

            resetCreateFields(true);
            remakeTable();

            showNotification("Success", "Succesfully added theme.");

        } else if (this.readyState === 4 && this.status === 500) {
            showNotification("Error", "Internal server error. Potentially the GUID is already in use.")
        }

    }
}

function readyToDelete(municipality_id, theme_id, row) {
    let msgModal = new bootstrap.Modal(document.getElementById('msgModal'));

    document.getElementById("modal_verification_message").innerHTML = "<div class=\"alert alert-warning\" role=\"alert\">\n" +
        "  Are you sure you want to delete this theme with municipality id: " + municipality_id + "<br>And GUID: " + theme_id + "?" +
        "</div>"

    let element = document.getElementById("confirm-yes");
    element.onclick = function () {
        let xhr = new XMLHttpRequest();
        xhr.open("DELETE", `/tomyappointment/rest/municipalities/${municipality_id}/themes/${theme_id}`);
        xhr.send();

        xhr.onreadystatechange = function () {
            if (this.readyState === 4 && this.status === 200) {
                row.remove();

                showNotification("Success", "Succesfully removed theme.");
            }
        }
    }

    msgModal.show();
}

function resetCreateMuniFields() {
    document.getElementById("muni_id").value = "";
    document.getElementById("muni_name").value = "";
    document.getElementById("muni_short_name").value = "";
}

function resetCreateFields(header) {

    if (header) {
        document.getElementById('createThemeModalLabel').innerHTML = "Create a new Theme";
        document.getElementById('save_theme').innerHTML = "Create";
        document.getElementById("theme_guid").disabled = false;
        document.getElementById("save_theme").onclick = function() {
            createTheme();
        }
    }

    document.getElementById("muni-dropdown").value = "0";
    document.getElementById("location_name").value = "";
    document.getElementById("home_url").value = "";
    document.getElementById("theme_guid").value = "";
    document.getElementById("bg_image").value = null;
    document.getElementById("logo_image").value = null;
    document.getElementById("icon_512").value = null;
    document.getElementById("icon_192").value = null;
    document.getElementById("font_family").value = "";
    document.getElementById("main_color").value = "#ff0000";
    document.getElementById("color_text").value = "#ff0000";
    document.getElementById("font_link").value = "";
    document.getElementById("font_file").value = null;
    document.getElementById("fontDefault").checked = true;
}

let doc_modal = document.getElementById('createThemeModal');
let editModal = new bootstrap.Modal(doc_modal);

function readyToEdit(muni_id, theme_id) {
    document.getElementById('createThemeModalLabel').innerHTML = "Edit an already existing theme";
    document.getElementById('save_theme').innerHTML = "Save Changes";
    document.getElementById('save_theme').onclick = function () {
        editTheme();
    }

    let http_req = new XMLHttpRequest();
    http_req.open("GET", "/tomyappointment/rest/municipalities/" + muni_id + "/themes/" + theme_id);
    http_req.send();

    http_req.onreadystatechange = function () {
        if (this.status === 200 && this.readyState === 4) {
            const data = JSON.parse(http_req.responseText);

            console.log(data);

            document.getElementById("muni-dropdown").value = data.municipality_id;
            document.getElementById("location_name").value = data.location;
            document.getElementById("home_url").value = data.home_url;
            document.getElementById("theme_guid").value = data.id;
            document.getElementById("theme_guid").disabled = true;
            document.getElementById("font_family").value = data.font_family;
            document.getElementById("main_color").value = data.colour;
            document.getElementById("color_text").value = data.colour;
            document.getElementById("font_link").value = data.font_url;

            document.getElementById("save_theme").onclick = function () {
                editTheme();
            }

            editModal.show();
        }
    }
}

function editTheme() {
    if (!doValidation("Cannot edit theme: ", false)) {
        return;
    }

    let muni_id = document.getElementById("muni-dropdown")?.value;

    let location = document.getElementById("location_name");

    let home_url = document.getElementById("home_url");

    let theme_id = document.getElementById("theme_guid")?.value;

    let background_image = document.getElementById("bg_image").files[0];

    let logo_image = document.getElementById("logo_image").files[0];

    let icon_image192 = document.getElementById("icon_192").files[0];

    let icon_image512 = document.getElementById("icon_512").files[0];

    let colour = document.getElementById("main_color");

    let font_family = document.getElementById("font_family")?.value;

    let radios = document.getElementsByName('font-setting');

    let formData = new FormData();

    formData.append("id", theme_id);
    formData.append("location", location?.value);
    formData.append("colour", colour?.value);
    formData.append("home_url", home_url?.value);

    if (background_image != null) {
        formData.append("background", background_image);
    }

    if (logo_image != null) {
        formData.append("logo", logo_image);
    }

    if (icon_image192 != null) {
        formData.append("icon-192", icon_image192);
    }

    if (icon_image512 != null) {
        formData.append("icon-512", icon_image512);
    }

    for (let i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            if (radios[i].value === "1") {
                let fontfile = document.getElementById("font_file").files[0];

                formData.append("font", fontfile);
                formData.append("font_family", font_family);

            } else if (radios[i].value === "2") {
                let fontlink = document.getElementById("font_link")?.value;

                formData.append("font_url", fontlink);
                formData.append("font_family", font_family);
            }
            break;
        }
    }

    let xhr = new XMLHttpRequest();

    xhr.open("PATCH", "/tomyappointment/rest/municipalities/" + muni_id + "/themes/" + theme_id);

    xhr.send(formData);

    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {

            $('#createThemeModal').modal('hide');

            resetCreateFields(true);
            remakeTable();

            showNotification("Success", "Succesfully edited theme.");
            editModal.hide();

        } else if (this.readyState === 4 && this.status === 500) {
            showNotification("Error", "Internal server error.")
        }

    }
}

function exportTheme(muni_id, theme_id) {
    window.location = `/tomyappointment/rest/municipalities/${muni_id}/themes/${theme_id}/export`;
}

function remakeTable() {
    const filter = document.querySelector("#search")?.value || "";
    console.log(filter);

    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/tomyappointment/rest/municipalities");
    xhr.onreadystatechange = () => {
        if (xhr.readyState === 4 && xhr.status === 200) {
            const data = JSON.parse(xhr.responseText);
            loadTable(data, filter);
        }
    }
    xhr.send();
}

function updateDropDown(doTable) {
    let dropdown = document.getElementById('muni-dropdown');
    dropdown.length = 0;

    let defaultOption = document.createElement('option');
    defaultOption.text = 'Choose a created municipality here';
    defaultOption.value = "0";

    dropdown.add(defaultOption);
    dropdown.selectedIndex = 0;

    let xhr_ = new XMLHttpRequest();
    xhr_.open('GET', "/tomyappointment/rest/municipalities", true);

    xhr_.onreadystatechange = function () {
        if (this.status === 200 && this.readyState === 4) {
            const data = JSON.parse(xhr_.responseText);

            Array.from(data).sort((a, b) => b.name.toLowerCase() >= a.name.toLowerCase() ? 0 : 1).map(item => {
                let option = document.createElement("option");
                option.text = item.name;
                option.value = item.id;

                return option;
            }).forEach(elem => dropdown.add(elem));

            if (doTable) {
                loadTable(data, "");
            }
        }
    }

    xhr_.onerror = function () {
        alert('An error occurred fetching the JSON');
    };

    xhr_.send();
}

let color_text = document.getElementById("color_text");
let color = document.getElementById("main_color");

color_text.addEventListener("input", function (e) {
    if (this.value.match(/^#([0-9a-f]{6})$/)) {
        color.value = this.value;
    } else if (this.value.match(/^#([0-9a-f]{3})$/)) {
        let r = this.value[1],
            g = this.value[2],
            b = this.value[3];
        color.value = `#${r}${r}${g}${g}${b}${b}`;
    }
})

color.addEventListener("input", function (e) {
    color_text.value = this.value;
})

$(document).ready(function () {
    $("input[name$='font-setting']").click(function () {
        let val = $(this).val();

        $("div.font-s").hide();

        if (val === "1") {
            $("#font-upload").show();
            $("#font-family").show();
        } else if (val === "2") {
            $("#font-link").show();
            $("#font-family").show();
        }
    });
});

$('#custom_guid_check').on('click', function () {
    $('#guid-enter').toggle();
});

updateDropDown(true);

function showNotification(header, text) {

    let color = "bg-dark";

    if (header === "Error") {
        color = "bg-danger";
    } else if (header === "Success") {
        color = "bg-success";
    }

    document.getElementById("toast-header").innerHTML = header;
    document.getElementById("toast-body").innerHTML = text;
    document.getElementById("header_notification").className = `toast-header bg-dark`;
    document.getElementById("notification").className = `toast hide ${color}`;
    $('#notification').toast('show');
}

function doValidation(prefix, for_preview) {

    if (!for_preview) {
        let muni_id = document.getElementById("muni-dropdown")?.value;
        if (muni_id.length !== 7) {
            showNotification("Error", prefix + "Please enter a municipality you want to add the theme for! " +
                "If there's no options, add it on the 'Create Municipality' option on the main screen.");
            return false;
        }

        let location = document.getElementById("location_name");
        if (location.value.length === 0) {
            showNotification("Error", prefix + "Please enter a location name for this specific municipality.");
            return false;
        }

        let home_url = document.getElementById("home_url");
        if (home_url.value.length === 0) {
            showNotification("Error", prefix + "Please enter a home url for this specific municipality.");
            return false;
        }

        let theme_id = document.getElementById("theme_guid")?.value;
        if (theme_id.length !== 36) {
            showNotification("Error", prefix + "The theme GUID is not of proper size! It requires 36 characters.");
            return false;
        }
    }

    if (document.getElementById("theme_guid").disabled !== true) {
        let background_image = document.getElementById("bg_image").files[0];
        if (background_image == null) {
            showNotification("Error", prefix + "There is no background image uploaded.");
            return false;
        } else {
            let allowedExtensions =
                /(\.jpg|\.jpeg)$/i;

            if (!allowedExtensions.exec(document.getElementById("bg_image").value)) {
                showNotification("Error", prefix + "Background image must have .jpg/.jpeg as file type.");
                return false;
            }
        }

        let logo_image = document.getElementById("logo_image").files[0];
        if (logo_image == null) {
            showNotification("Error", prefix + "There is no logo image uploaded.");
            return false;
        } else {
            let allowedExtensions =
                /(\.png)$/i;

            if (!allowedExtensions.exec(document.getElementById("logo_image").value)) {
                showNotification("Error", prefix + "Logo image must have .png as file type.");
                return false;
            }
        }
        let icon_image192 = document.getElementById("icon_192").files[0];
        if (icon_image192 == null) {
            showNotification("Error", prefix + "There is no icon image for 192x192 uploaded.");
            return false;
        } else {
            let allowedExtensions =
                /(\.png)$/i;

            if (!allowedExtensions.exec(document.getElementById("icon_192").value)) {
                showNotification("Error", prefix + "Icon image 192x192 must have .png as file type.");
                return false;
            }
        }

        let icon_image512 = document.getElementById("icon_512").files[0];
        if (icon_image512 == null) {
            showNotification("Error", prefix + "There is no icon image for 512x512 uploaded.");
            return false;
        } else {
            let allowedExtensions =
                /(\.png)$/i;

            if (!allowedExtensions.exec(document.getElementById("icon_512").value)) {
                showNotification("Error", prefix + "Icon image 512x512 must have .png as file type.");
                return false;
            }
        }
    }

    let colour = document.getElementById("main_color");
    if (colour.value.length === 0) {
        showNotification("Error", prefix + "Oops, you've cleared the Hex field for the colour! Please enter it again.");
        return false;
    }

    let radios = document.getElementsByName('font-setting');
    let font_family = document.getElementById("font_family")?.value;

    for (let i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            if (radios[i].value === "1") {
                let fontfile = document.getElementById("font_file").files[0];
                if (fontfile == null) {
                    showNotification("Error", prefix + "Please upload the font for your font option.")
                    return false;
                } else {
                    let allowedExtensions =
                        /(\.ttf|.otf)$/i;

                    if (!allowedExtensions.exec(document.getElementById("font_file").value)) {
                        showNotification("Error", prefix + "Font upload must have .ttf or .otf as extension!");
                        return false;
                    }
                }
                if (font_family.length === 0) {
                    showNotification("Error", prefix + "Please enter the corresponding font family to your font upload or font link.")
                    return false;
                }

            } else if (radios[i].value === "2") {
                let fontlink = document.getElementById("font_link")?.value;

                if (fontlink.length === 0 || !fontlink.toLowerCase().startsWith("http")) {
                    showNotification("Error", prefix + "Please enter a correct font link for your font option (starting with http).")
                    return false;
                }

                if (font_family.length === 0) {
                    showNotification("Error", prefix + "Please enter the corresponding font family to your font link.")
                    return false;
                }
            }
            break;
        }
    }

    return true;
}

let previewModalDoc = document.getElementById('previewModal');
let previewModal = new bootstrap.Modal(previewModalDoc);

function setupPreview() {

    if (doValidation("For previewing: ", true)) {
        let preview_background;
        let preview = document.getElementById("prev_bod");

        // we can assume all these values are set.
        let set_color = document.getElementById("main_color")?.value;
        let bg_image = document.getElementById("bg_image")?.files[0];

        if (bg_image === null || bg_image === undefined){
            let muni_id = document.getElementById("muni-dropdown")?.value;
            let theme_id = document.getElementById("theme_guid")?.value;
            preview_background = `url("resources/${muni_id}/${theme_id}/background.jpg")`;
        } else {
            preview_background = `url("${URL.createObjectURL(bg_image)}")`;
        }

        preview.style.backgroundImage = preview_background;

        let logo_image = document.getElementById("logo_image")?.files[0];
        if (logo_image === null || logo_image === undefined){
            let muni_id = document.getElementById("muni-dropdown")?.value;
            let theme_id = document.getElementById("theme_guid")?.value;
            document.getElementById("preview_logoImg").src = `resources/${muni_id}/${theme_id}/logo.png`;

        } else {
            document.getElementById("preview_logoImg").src = URL.createObjectURL(logo_image);
        }

        let font_family = document.getElementById("font_family")?.value;
        let fontlink = document.getElementById("font_link")?.value;
        let fontfile = document.getElementById("font_file")?.value;

        document.getElementById("preview_text_date").style.color = set_color;

        let buttons = document.getElementsByClassName("preview_button");
        Array.from(buttons).forEach((btn) => {
            btn.style.backgroundColor = set_color;
        });

        let radios = document.getElementsByName('font-setting');

        for (let i = 0, length = radios.length; i < length; i++) {
            if (radios[i].checked) {
                if (radios[i].value === "1") {
                    // font upload
                    document.documentElement.style.setProperty('--font-family-preview', font_family)
                } else if (radios[i].value === "2") {
                    // font link
                    document.documentElement.style.setProperty('--font-family-preview', font_family)
                }
                break;
            }
        }

        previewModal.show();

    }

}

