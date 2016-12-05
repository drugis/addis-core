'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', '$location', '$modal',
    'ProjectResource',
    'TrialverseResource',
    'TrialverseStudyResource',
    'SemanticOutcomeResource',
    'OutcomeResource',
    'SemanticInterventionResource',
    'InterventionResource',
    'CovariateOptionsResource',
    'CovariateResource',
    'AnalysisResource',
    'ANALYSIS_TYPES',
    'InterventionService',
    'activeTab',
    'UserService',
    'ReportResource'
  ];
  var SingleProjectController = function($scope, $q, $state, $stateParams, $location, $modal, ProjectResource, TrialverseResource,
    TrialverseStudyResource, SemanticOutcomeResource, OutcomeResource, SemanticInterventionResource, InterventionResource,
    CovariateOptionsResource, CovariateResource, AnalysisResource, ANALYSIS_TYPES, InterventionService, activeTab, UserService, ReportResource) {
    $scope.activeTab = activeTab;

    $scope.analysesLoaded = false;
    $scope.covariatesLoaded = true;
    $scope.loading = {
      loaded: false
    };
    $scope.editMode = {
      allowEditing: false
    };
    $scope.duplicateOutcomeName = {
      isDuplicate: false
    };
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.userId = $stateParams.userUid;

    $scope.projects = ProjectResource.query();
    $scope.project = ProjectResource.get($stateParams);

    // load project
    $scope.project.$promise.then(function() {
      $scope.loading.loaded = true;

      if (UserService.isLoginUserId($scope.project.owner.id)) {
        $scope.editMode.allowEditing = true;
      }

      $scope.trialverse = TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.semanticOutcomes = SemanticOutcomeResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.semanticInterventions = SemanticInterventionResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.outcomes = OutcomeResource.query({
        projectId: $scope.project.id
      });

      InterventionResource.query({
        projectId: $scope.project.id
      }).$promise.then(generateInterventionDescriptions);

      loadCovariates();

      $scope.studies = TrialverseStudyResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.studies.$promise.then(function() {
        $scope.analyses = AnalysisResource.query({
          projectId: $scope.project.id
        }, function(analyses) {
          $scope.analysesLoaded = true;
          $scope.analyses = _.sortBy(analyses, function(analysis) {
            if (analysis.analysisType === 'Evidence synthesis') {
              return 0;
            } else {
              return 1;
            }
          });
        });
      });
      $scope.reportText = ReportResource.get($stateParams);

      $scope.reportText.$promise.then(function() {
        if ($scope.reportText.data.localeCompare('default report text') === 0) {
          $scope.showLegacyReport = true;
        }
      });
    });

    function generateInterventionDescriptions(interventions) {
      $scope.interventions = interventions.map(function(intervention) {
        intervention.definitionLabel = InterventionService.generateDescriptionLabel(intervention, interventions);
        return intervention;
      });
      $scope.interventions = _.orderBy($scope.interventions, function(intervention) {
        return intervention.name.toLowerCase();
      });
    }

    function loadCovariates() {
      // we need to get the options in order to display the definition label, as only the definition key is stored on the covariate
      $q.all([CovariateOptionsResource.getProjectCovariates($stateParams).$promise,
        CovariateResource.query({
          projectId: $scope.project.id
        }).$promise
      ]).then(function(result) {
        var optionsMap = _.keyBy(result[0], 'key');
        $scope.covariates = result[1].map(function(covariate) {
          covariate.definitionLabel = optionsMap[covariate.definitionKey].label;
          return covariate;
        });
      });
    }

    $scope.findStudyLabel = function(analysis) {
      var study = _.find($scope.studies, function(study) {
        return 'http://trials.drugis.org/graphs/' + study.studyGraphUid === analysis.studyGraphUri;
      });
      if (study) {
        return study.name;
      }
    };

    $scope.goToAnalysis = function(analysis) {
      var analysisType = angular.copy(_.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysis.analysisType;
      }));

      //todo if analysis is gemtc type and has a problem go to models view
      if (analysisType.label === 'Benefit-risk analysis based on meta-analyses' && analysis.finalized) {
        analysisType.stateName = 'metaBenefitRisk';
      }

      $state.go(analysisType.stateName, {
        userUid: $scope.userId,
        projectId: $scope.project.id,
        analysisId: analysis.id
      });
    };

    $scope.openEditProjectDialog = function() {
      $modal.open({
        templateUrl: './app/js/project/editProject.html',
        controller: 'EditProjectController',
        resolve: {
          project: function() {
            return $scope.project;
          },
          otherProjectNames: function() {
            return $scope.projects.$promise.then(function(projects) {
              return _.reduce(projects, function(accum, project) {
                if ($scope.project.id !== project.id) {
                  accum.push(project.name);
                }
                return accum;
              }, []);
            });
          },
          callback: function() {
            return function(name, description) {
              $scope.project.name = name;
              $scope.project.description = description;
            };
          }
        }
      });
    };

    $scope.openAddAnalysisDialog = function() {
      $modal.open({
        templateUrl: './app/js/analysis/addAnalysis.html',
        scope: $scope,
        controller: 'AddAnalysisController'
      });
    };

    $scope.openCreateOutcomeDialog = function() {
      $modal.open({
        templateUrl: './app/js/outcome/addOutcome.html',
        scope: $scope,
        controller: 'AddOutcomeController',
        resolve: {
          callback: function() {
            return function() {
              $scope.outcomes = OutcomeResource.query({
                projectId: $scope.project.id
              });
            };
          }
        }
      });
    };

    $scope.openCreateInterventionDialog = function() {
      $modal.open({
        templateUrl: './app/js/intervention/addIntervention.html',
        scope: $scope,
        controller: 'AddInterventionController',
        resolve: {
          callback: function() {
            return function() {
              $scope.interventions = InterventionResource.query({
                projectId: $scope.project.id
              }).$promise.then(generateInterventionDescriptions);
            };
          }
        }
      });
    };

    $scope.openEditOutcomeDialog = function(outcome) {
      $modal.open({
        templateUrl: './app/js/outcome/editOutcome.html',
        controller: 'EditAddisOutcomeController',
        resolve: {
          outcome: function() {
            return outcome;
          },
          outcomes: function() {
            return $scope.outcomes;
          },
          successCallback: function() {
            return function() {
              $scope.outcomes = OutcomeResource.query({
                projectId: $scope.project.id
              });
            };
          }
        }
      });
    };

    $scope.openEditInterventionDialog = function(intervention) {
      $modal.open({
        templateUrl: './app/js/intervention/editIntervention.html',
        controller: 'EditInterventionController',
        resolve: {
          intervention: function() {
            return intervention;
          },
          interventions: function() {
            return $scope.interventions;
          },
          successCallback: function() {
            return function() {
              $scope.interventions = InterventionResource.query({
                projectId: $scope.project.id
              }).$promise.then(generateInterventionDescriptions);
            };
          }
        }
      });
    };

    $scope.addCovariate = function() {
      $modal.open({
        templateUrl: './app/js/covariates/addCovariate.html',
        scope: $scope,
        controller: 'AddCovariateController',
        resolve: {
          outcomes: function() {
            return $scope.outcomes;
          },
          callback: function() {
            return loadCovariates;
          }
        }
      });
    };

    $scope.setActiveTab = function(tab) {
      if (tab === $scope.activeTab) {
        return;
      }
      $scope.activeTab = tab;
      var path = $location.path();
      if (tab === 'report') {
        $location.path(path + '/report');
      } else if (tab === 'editedReport') {
        $location.path(path + '/editedReport');
      } else {
        var newPath = path.substring(0, path.length - '/report'.length);
        $location.path(newPath);
      }
    };

    $scope.goToEditView = function() {
      $state.go('editReport', $stateParams);
    };

  };
  return dependencies.concat(SingleProjectController);
});
