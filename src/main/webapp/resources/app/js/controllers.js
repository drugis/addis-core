'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.controllers', [])
    .controller('ProjectsController', require('controllers/projectsController'))
    .controller('SingleProjectController', require('controllers/singleProjectController'))
    .controller('SingleStudyBenefitRiskAnalysisController', require('controllers/singleStudyBenefitRiskAnalysisController'))
    .controller('NetworkMetaAnalysisController', require('controllers/networkMetaAnalysisController'));
});
