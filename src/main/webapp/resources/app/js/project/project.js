'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    ;
});
