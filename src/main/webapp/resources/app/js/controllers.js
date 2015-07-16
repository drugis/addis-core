'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.controllers', [])
    .controller('ProjectsController', require('controllers/projectsController'))
    .controller('CreateProjectController', require('controllers/createProjectController'))
    .controller('SingleProjectController', require('controllers/singleProjectController'))
    .controller('SingleStudyBenefitRiskAnalysisController', require('controllers/singleStudyBenefitRiskAnalysisController'))
    .controller('NetworkMetaAnalysisController', require('controllers/networkMetaAnalysisController'))
    .controller('NamespaceController', require('controllers/namespaceController'))
    .controller('StudyController', require('controllers/studyController'));
});
