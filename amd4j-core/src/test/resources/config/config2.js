requirejs.config({
  shim: {
    'jquery.colorize': ['jquery'],
    'jquery.scroll': ['jquery'],
    'backbone.layoutmanager': ['backbone']
  },
  paths : {
    "jquery" : [
        "https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min",
        "js/components/external/jquery" ]
  }
});