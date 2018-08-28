'use strict';
define([
  './editAddisOutcomeController',
  'angular'
], function(
  EditAddisOutcomeController,
  angular
) {
    var dependencies = [];

    return angular.module('addis.outcomes', dependencies)
      // controllers
      .controller('EditAddisOutcomeController', EditAddisOutcomeController)

      //services

      //filter
      ;
  }
);
