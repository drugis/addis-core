'use strict';
var requires = [
  'excelExport/excelExportService',
  'excelExport/excelExportUtilService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  ExcelExportService,
  ExcelExportUtilService,
  angular
) {
  return angular.module('addis.excelExport', ['ngResource',
      'trialverse.study'
    ])
    //services
    .factory('ExcelExportService', ExcelExportService)
    .factory('ExcelExportUtilService', ExcelExportUtilService);
});