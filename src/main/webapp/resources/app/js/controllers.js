'use strict';
define(function(require) {
  var angular = require('angular');
    return angular.module('addis.controllers', [])
          .controller('ProjectsController', require('controllers/projectsController'));
});
