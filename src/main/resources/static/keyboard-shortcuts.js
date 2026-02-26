"use strict";

htmx.onLoad(() => {
    const createPostForm = htmx.find("#post-form>form");
    htmx.on(createPostForm, "keyup", (e) => {
        if (!e.ctrlKey || !(e.key === "Enter")) {
            return;
        }
        e.preventDefault();
        htmx.trigger(createPostForm, "createPost");
    });
});
