'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('addis.project', ['ngResource'])
    // resources
    .factory('ReportResource', require('project/reportResource'))

    // controllers
    .controller('CreateProjectModalController', require('project/createProjectModalController'))
    .controller('EditProjectController', require('project/editProjectController'))
    .controller('EditReportController', require('project/editReportController'))

    //directives
    .directive('nmaReportView', require('project/nmaReportViewDirective'))
    .directive('ssbrReportView', require('project/ssbrReportViewDirective'))

    .service('ProjectService', require('project/projectService'))
    ;
});
