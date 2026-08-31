"use strict";

htmx.onLoad(() => {
    htmx.on("htmx:configRequest", (e) => {
        const csrfToken = htmx.find('meta[name="_csrf"]').getAttribute("content");
        const csrfHeader = htmx.find('meta[name="_csrf_header"]').getAttribute("content");
        e.detail.headers[csrfHeader] = csrfToken;
    });
    htmx.on("htmx:beforeSwap", ({ detail }) => {
        if (detail.isError && !detail.shouldSwap) {
            detail.shouldSwap = true;
        }
    });
});
