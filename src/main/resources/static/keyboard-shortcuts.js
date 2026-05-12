"use strict";

htmx.onLoad(() => {
    const createPostForm = htmx.find("#postform-wrap>form");
    if (!!createPostForm) {
        htmx.on(createPostForm, "keyup", (e) => {
            if (!e.ctrlKey || !(e.key === "Enter")) {
                return;
            }
            e.preventDefault();
            htmx.trigger(createPostForm, "createPost");
        });
    }
});
