'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = [];

  return angular.module('addis.outcomes',
    dependencies)
    // controllers
    .controller('EditAddisOutcomeController', require('outcome/editAddisOutcomeController'))

    //services


    //filter
    ;
});
