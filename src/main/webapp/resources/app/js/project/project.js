'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))

    //directives
    .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))

    .service('ProjectService', require('project/projectService'))
    ;
});
