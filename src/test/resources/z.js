//my/shirt.js now does setup work
//before returning its module definition.
define(["x", "js/y", "q"], function ($, _, Backbone) {
    //Do setup work here
    return {
        color: "black",
        size: "unisize"
    }
});
