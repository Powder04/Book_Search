const socket = new SockJS('/ws', null, { transports: ['websocket'] });
const stompClient = Stomp.over(socket);

let subscription = null;
let connected = false;
let currentKeyword = "";
let translatedKeyword = "";

const PAGE_SIZE = 10;

const SOURCES = [
    { name: "Open Library",      ul: "openlibrary",      pagination: "pagination-open",     summary: "sum-open" },
    { name: "Standard EBooks",   ul: "standardebooks",   pagination: "pagination-standard", summary: "sum-standard" },
    { name: "Project Gutenberg", ul: "projectgutenberg", pagination: "pagination-project",  summary: "sum-project" },
    { name: "VOER",              ul: "voer",             pagination: "pagination-voer",     summary: "sum-voer" }
];

const sourceConfigByName = Object.fromEntries(SOURCES.map(s => [s.name, s]));

function initCurrentPages() {
    return Object.fromEntries(SOURCES.map(s => [s.name, 1]));
}

function initBooksBySource() {
    return Object.fromEntries(SOURCES.map(s => [s.name, []]));
}

let currentPages = initCurrentPages();

// Cac open resource de render theo
let booksBySource = initBooksBySource();

const loading = document.getElementById("loader");

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

            booksBySource = initBooksBySource();
            currentPages = initCurrentPages();

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
            console.log("Current Keyword:", currentKeyword);
            console.log("Translated Keyword:", translatedKeyword);

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

function getLanguageBucket(book) {
    const lang = (book.language || "").trim().toLowerCase();
    if (!lang) return "other";

    const viCodes = ["vi", "vie", "vietnamese"];
    const enCodes = ["en", "eng", "english"];

    if (viCodes.includes(lang)) return "vi";
    if (enCodes.includes(lang)) return "en";
    return "other";
}

function filterByLanguage(books) {
    const checked = Array.from(document.querySelectorAll('input[name="language"]:checked'))
        .map(el => el.value);

    if (checked.length === 0) return books; // Khong chon gi -> khong loc

    return books.filter(book => checked.includes(getLanguageBucket(book)));
}

function parsePublishDate(publishYear) {
    if (!publishYear) return null;
    const str = String(publishYear).trim();

    // dd/mm/yyyy - VOER hien theo dinh dang ngay/thang/nam Viet Nam
    let m = str.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if (m) {
        const [, d, mo, y] = m;
        return new Date(Number(y), Number(mo) - 1, Number(d)).getTime();
    }

    // yyyy-mm-dd (co the kem gio:phut:giay theo sau)
    m = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/);
    if (m) {
        const [, y, mo, d] = m;
        return new Date(Number(y), Number(mo) - 1, Number(d)).getTime();
    }

    // Chi co nam (vd Open Library tra "2013")
    if (/^\d{4}$/.test(str)) {
        return new Date(Number(str), 0, 1).getTime();
    }

    // Du phong: neu co bat ky nam 4 chu so nao trong chuoi thi lay tam nam do (1/1)
    m = str.match(/\d{4}/);
    return m ? new Date(Number(m[0]), 0, 1).getTime() : null;
}

function sortByPublishDate(books) {
    const mode = document.querySelector('input[name="value-publish"]:checked')?.value || "newest";

    const withDate = [];
    const withoutDate = [];

    books.forEach(book => {
        const ts = parsePublishDate(book.publishYear);
        if (ts === null) withoutDate.push(book);
        else withDate.push(book);
    });

    withDate.sort((a, b) => {
        const tsA = parsePublishDate(a.publishYear);
        const tsB = parsePublishDate(b.publishYear);
        return mode === "oldest" ? tsA - tsB : tsB - tsA;
    });

    return mode === "oldest" ? [...withoutDate, ...withDate] : [...withDate, ...withoutDate];
}

function renderSource(source) {

    const config = sourceConfigByName[source];
    if(!config) return;

    const ul = document.getElementById(config.ul);
    if(!ul) return;

    ul.innerHTML = "";

    const mode = document.querySelector('input[name="value-title"]:checked')?.value || "value-1";
    let books = booksBySource[source] || [];
    if(mode !== "value-1") books = filterAdvanced(books);
    books = filterByLanguage(books);
    books = sortByPublishDate(books);

    const page = currentPages[source] || 1;
    const start = (page - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;

    const paginated = books.slice(start, end);

    paginated.forEach(displayBook);

    renderPagination(source, books.length);
}

function renderPagination(source, totalItems) {

    const config = sourceConfigByName[source];
    if(!config) return;

    const totalPages = Math.ceil(totalItems / PAGE_SIZE);
    const currentPage = currentPages[source];

    const container = document.getElementById(config.pagination);
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

    const config = sourceConfigByName[book.source];
    if(!config) return;

    const ul = document.getElementById(config.ul);
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
    SOURCES.forEach(s => {
        const ul = document.getElementById(s.ul);
        if(ul) ul.innerHTML = "";
    });
}

function showLoading() {
    loading.classList.remove("hidden");
}
function hiddenLoading() {
    loading.classList.add("hidden");
}

function getBooksToShow() {
    const mode = document.querySelector('input[name="value-title"]:checked')?.value || "value-1";
    let all = [];

    Object.values(booksBySource).forEach(list => {
        all = all.concat(list);
    });

    const filtered = mode === "value-1" ? all : filterAdvanced(all);
    return filterByLanguage(filtered);
}

function updateSummary() {
    const books = getBooksToShow();
    const count = Object.fromEntries(SOURCES.map(s => [s.name, 0]));

    books.forEach(book => {
        if(count[book.source] !== undefined) {
            count[book.source]++;
        }
    });

    SOURCES.forEach(s => {
        const el = document.getElementById(s.summary);
        if(el) el.textContent = `${s.name} (${count[s.name]})`;
    });
}

document.querySelectorAll('input[name="value-title"]').forEach(radio => {
    radio.addEventListener("change", () => {
        Object.keys(booksBySource).forEach(renderSource);
        updateSummary();
    });
});

document.querySelectorAll('input[name="language"]').forEach(checkbox => {
    checkbox.addEventListener("change", () => {
        Object.keys(booksBySource).forEach(renderSource);
        updateSummary();
    });
});

document.querySelectorAll('input[name="value-publish"]').forEach(radio => {
    radio.addEventListener("change", () => {
        Object.keys(booksBySource).forEach(renderSource);
    });
});