//before returning its module definition.
define(['text!./y.html', 'text!./yy.html', 'text!./y.xml', './v'], function () {
  return function () {
    return "y";
  };
});