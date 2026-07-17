const socket = new SockJS('/ws', null, { transports: ['websocket'] });
const stompClient = Stomp.over(socket);

let subscription = null;
let connected = false;
let currentKeyword = "";
let translatedKeyword = "";

const PAGE_SIZE = 10;
let currentPages = {
    "Open Library": 1,
    "Standard EBooks": 1,
    "Project Gutenberg": 1,
    "VOER": 1
};

const loading = document.getElementById("loader");

// Cac open resource de render theo
let booksBySource = {
    "Open Library": [],
    "Standard EBooks": [],
    "Project Gutenberg": [],
    "VOER": []
};

stompClient.connect({}, () => {
    connected = true;
    console.log("WebSocket connected");
});

// Bat event khi User gui keyword
document.getElementById("keyword").addEventListener("keypress", async (e) => {
    if(e.key === "Enter") {

        if(!connected) {
            alert("WebSocket chưa kết nối");
            return;
        }

        try {
            const keyword = e.target.value;
            showLoading();

            booksBySource = {
                "Open Library": [],
                "Standard EBooks": [],
                "Project Gutenberg": [],
                "VOER": []
            };

            currentPages = {
                "Open Library": 1,
                "Standard EBooks": 1,
                "Project Gutenberg": 1,
                "VOER": 1
            };

            clearUI();

            const response = await fetch("/search", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: "keyword=" + encodeURIComponent(keyword)
            });

            const filter = await response.json();
            const searchId = filter.searchId;
            currentKeyword = filter.keyword;
            translatedKeyword = filter.keywordTrans;

            console.log("Search ID:", searchId);

            if(subscription) {
                subscription.unsubscribe();
            }

            subscription = stompClient.subscribe("/list/search/" + searchId, (message) => {
                const data = JSON.parse(message.body);

                if(data.type === "DONE") {
                    hiddenLoading();
                    return;
                }

                // Push theo source
                if(!booksBySource[data.source]) {
                    booksBySource[data.source] = [];
                }

                booksBySource[data.source].push(data);

                // Render tung source
                renderSource(data.source);
                updateSummary();
            });

        } catch(error) {
            console.log(error);
        }
    }
});

function renderSource(source) {

    const ulMap = {
        "Open Library": "openlibrary",
        "Standard EBooks": "standardebooks",
        "Project Gutenberg": "projectgutenberg",
        "VOER": "voer"
    };

    const ul = document.getElementById(ulMap[source]);
    if(!ul) return;

    ul.innerHTML = "";

    const mode = document.querySelector('input[name="value-radio"]:checked')?.value || "value-1";
    let books = booksBySource[source] || [];
    if(mode !== "value-1") books = filterAdvanced(books);

    const page = currentPages[source] || 1;
    const start = (page - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;

    const paginated = books.slice(start, end);

    paginated.forEach(displayBook);

    renderPagination(source, books.length);
}

function renderPagination(source, totalItems) {

    const totalPages = Math.ceil(totalItems / PAGE_SIZE);
    const currentPage = currentPages[source];

    const containerMap = {
        "Open Library": "pagination-open",
        "Standard EBooks": "pagination-standard",
        "Project Gutenberg": "pagination-project",
        "VOER": "pagination-voer"
    };

    const container = document.getElementById(containerMap[source]);
    if(!container) return;

    container.innerHTML = "";

    if(totalPages <= 1) return;

    const createBtn = (label, page, disabled = false, isActive = false) => {
        const btn = document.createElement("button");
        btn.textContent = label;

        if(disabled) btn.disabled = true;
        if(isActive) btn.classList.add("active");

        btn.onclick = () => {
            if (!disabled && page !== null) {
                currentPages[source] = page;
                renderSource(source);
            }
        };

        return btn;
    };

    container.appendChild(
        createBtn("←", currentPage - 1, currentPage === 1)
    );

    const pages = [];
    pages.push(1);

    const start = Math.max(2, currentPage - 1);
    const end = Math.min(totalPages - 1, currentPage + 1);

    if(start > 2) pages.push("...");

    for(let i = start; i <= end; i++) {
        pages.push(i);
    }

    if(end < totalPages - 1) pages.push("...");

    if(totalPages > 1) pages.push(totalPages);

    pages.forEach(p => {
        if(p === "...") {
            const span = document.createElement("span");
            span.textContent = "...";
            span.style.margin = "0 5px";
            container.appendChild(span);
        } else {
            container.appendChild(
                createBtn(p, p, false, p === currentPage)
            );
        }
    });

    container.appendChild(
        createBtn("→", currentPage + 1, currentPage === totalPages)
    );
}

function displayBook(book) {

    const map = {
        "Open Library": "openlibrary",
        "Standard EBooks": "standardebooks",
        "Project Gutenberg": "projectgutenberg",
        "VOER": "voer"
    };

    const ul = document.getElementById(map[book.source]);
    if(!ul) return;

    const li = document.createElement("li");
    li.className = "book-card";

    li.innerHTML = `
        <img src="${book.coverUrl || '/images/CTU_logo_singlecolor.png'}" class="book-cover">
        <div class="book-info">
            <h3 class="book-title">
                <a href="${book.url}" target="_blank">${book.title}</a>
            </h3>
            <p><b>Author:</b> ${book.author || "Unknown"}</p>
            <p><b>Year:</b> ${book.publishYear || "N/A"}</p>
            <p><b>Language:</b> ${book.language || "N/A"}</p>
            <p><b>Source:</b> ${book.source}</p>
        </div>
    `;

    ul.appendChild(li);
}

function filterAdvanced(books) {
    const keyword = normalize(currentKeyword);
    const keywordTrans = normalize(translatedKeyword);

    return books.filter(book => {
        const title = normalize(book.title || "");
        return title.includes(keyword) || title.includes(keywordTrans);
    });
}

function normalize(text) {
    return text.toLowerCase()
        .replace(/[-_]/g, " ")
        .replace(/\s+/g, " ")
        .trim();
}

function clearUI() {
    document.getElementById("openlibrary").innerHTML = "";
    document.getElementById("standardebooks").innerHTML = "";
    document.getElementById("projectgutenberg").innerHTML = "";
    document.getElementById("voer").innerHTML = "";
}

function showLoading() {
    loading.classList.remove("hidden");
}
function hiddenLoading() {
    loading.classList.add("hidden");
}

function getBooksToShow() {
    const mode = document.querySelector('input[name="value-radio"]:checked')?.value || "value-1";
    let all = [];

    Object.values(booksBySource).forEach(list => {
        all = all.concat(list);
    });

    return mode === "value-1" ? all : filterAdvanced(all);
}

function updateSummary() {
    const books = getBooksToShow();
    const count = {
        "Open Library": 0,
        "Standard EBooks": 0,
        "Project Gutenberg": 0,
        "VOER": 0
    };

    books.forEach(book => {
        if(count[book.source] !== undefined) {
            count[book.source]++;
        }
    });

    document.getElementById("sum-open").textContent = `Open Library (${count["Open Library"]})`;
    document.getElementById("sum-standard").textContent = `Standard EBooks (${count["Standard EBooks"]})`;
    document.getElementById("sum-project").textContent = `Project Gutenberg (${count["Project Gutenberg"]})`;
    document.getElementById("sum-voer").textContent = `VOER (${count["VOER"]})`;
}

document.querySelectorAll('input[name="value-radio"]').forEach(radio => {
    radio.addEventListener("change", () => {
        Object.keys(booksBySource).forEach(renderSource);
        updateSummary();
    });
});