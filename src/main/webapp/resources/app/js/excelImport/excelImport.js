'use strict';
var requires = [
  'excelImport/excelImportService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  ExcelImportService,
  angular
) {
  return angular.module('addis.excelImport', ['ngResource',
      'trialverse.study'
    ])
    //services
    .factory('ExcelImportService', ExcelImportService)
    ;
});