'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', '$location', '$modal',
    'ProjectResource',
    'ProjectService',
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
    'ReportResource',
    'HistoryResource',
    'project'
  ];
  var SingleProjectController = function($scope, $q, $state, $stateParams, $location, $modal, ProjectResource, ProjectService,
    TrialverseResource,
    TrialverseStudyResource, SemanticOutcomeResource, OutcomeResource, SemanticInterventionResource, InterventionResource,
    CovariateOptionsResource, CovariateResource, AnalysisResource, ANALYSIS_TYPES, InterventionService, activeTab, UserService,
    ReportResource, HistoryResource, project) {
    $scope.tabSelection = {
      activeTab: activeTab
    };

    $scope.analysesLoaded = false;
    $scope.covariatesLoaded = true;
    $scope.showArchived = false;
    $scope.numberOfAnalysesArchived = 0;
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

    $scope.project = project;
    $scope.projects = ProjectResource.query();

    // load project
    $scope.loading.loaded = true;

    $scope.editMode.allowEditing = !project.archived && UserService.isLoginUserId($scope.project.owner.id);
    $scope.editMode.allowCopying = UserService.hasLoggedInUser();

    $scope.trialverse = TrialverseResource.get({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.trialverse.$promise.then(function(dataset) {
      $scope.currentRevision = HistoryResource.get({
        userUid: $scope.userId,
        datasetUuid: $scope.project.namespaceUid,
        versionUuid: dataset.version.split('/versions/')[1]
      });
    });

    $scope.semanticOutcomes = SemanticOutcomeResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.semanticInterventions = SemanticInterventionResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    $scope.studies = TrialverseStudyResource.query({
      namespaceUid: $scope.project.namespaceUid,
      version: $scope.project.datasetVersion
    });

    loadAnalyses();

    $scope.reportText = ReportResource.get($stateParams);
    $scope.reportText.$promise.then(function() {
      if ($scope.reportText.data.localeCompare('default report text') === 0) {
        $scope.showLegacyReport = true;
      } else {
        $scope.showLegacyReport = false;
      }
    });


    function reloadDefinitions() {
      loadCovariates();
      loadInterventions();
      loadOutcomes();
    }

    function loadAnalyses() {
      $scope.studies.$promise.then(function() {
        $scope.analyses = AnalysisResource.query({
          projectId: $scope.project.id
        }).$promise.then(function(analyses) {
          $scope.analysesLoaded = true;
          $scope.numberOfAnalysesArchived = _.reduce(analyses, function(accum, analysis) {
            return analysis.archived ? ++accum : accum;
          }, 0);
          if ($scope.numberOfAnalysesArchived === 0) {
            $scope.showArchived = false;
          }
          $scope.analyses = _.sortBy(analyses, function(analysis) {
            if (analysis.analysisType === 'Evidence synthesis' && analysis.archived === false) {
              return 0;
            } else if (analysis.archived === false) {
              return 1;
            } else {
              return 2;
            }
          });
          reloadDefinitions();
        });
      });
    }

    function loadInterventions() {
      InterventionResource.query({
          projectId: $scope.project.id
        }).$promise
        .then(generateInterventionDescriptions)
        .then(function() {
          $scope.interventionUsage = ProjectService.buildInterventionUsage($scope.analyses, $scope.interventions);
        });
    }

    function loadOutcomes() {
      $scope.outcomes = OutcomeResource.query({
        projectId: $scope.project.id
      });
      $scope.outcomes.$promise.then(function(value) {
        $scope.outcomes = value;
      });
      $scope.outcomes.$promise.then(function() {
        $scope.outcomeUsage = ProjectService.buildOutcomeUsage($scope.analyses, $scope.outcomes);
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
      }).then(function() {
        $scope.covariateUsage = ProjectService.buildCovariateUsage($scope.analyses, $scope.covariates);
      });
    }

    function generateInterventionDescriptions(interventions) {
      $scope.interventions = interventions.map(function(intervention) {
        intervention.definitionLabel = InterventionService.generateDescriptionLabel(intervention, interventions);
        return intervention;
      });
      $scope.interventions = _.orderBy($scope.interventions, function(intervention) {
        return intervention.name.toLowerCase();
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
            return loadOutcomes;
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
            return loadInterventions;
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
          usage: function() {
            return $scope.outcomeUsage[outcome.id];
          },
          successCallback: function() {
            return loadOutcomes;
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
            return loadInterventions;
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
      if (tab === $scope.tabSelection.activeTab) {
        return;
      }
      if (tab === 'report') {
        $state.go('projectReport', $stateParams);
      } else if (tab === 'definitions') {
        $state.go('projectDefinitions', $stateParams);
      } else if(tab === 'analyses'){
        $state.go('project', $stateParams, {
          reload: true
        });
      }
    };

    $scope.goToEditView = function() {
      $state.go('editReport', $stateParams);
    };

    $scope.openDeleteCovariateDialog = function(covariate) {
      $modal.open({
        templateUrl: './app/js/project/deleteDefinition.html',
        scope: $scope,
        controller: 'DeleteDefinitionController',
        resolve: {
          definition: function() {
            return _.assign({}, covariate, {
              definitionType: 'covariate'
            });
          },
          callback: function() {
            return loadCovariates;
          }
        }
      });
    };

    $scope.openDeleteDefinitionDialog = function(definition, type) {
      $modal.open({
        templateUrl: './app/js/project/deleteDefinition.html',
        scope: $scope,
        controller: 'DeleteDefinitionController',
        resolve: {
          definition: function() {
            return _.assign({}, definition, {
              definitionType: type
            });
          },
          callback: function() {
            return reloadDefinitions;
          }
        }
      });
    };
    $scope.openUpdateDialog = function() {
      $modal.open({
        templateUrl: './app/js/project/updateProject.html',
        scope: $scope,
        controller: 'UpdateProjectController',
        resolve: {
          callback: function() {
            return function(newProjectId) {
              ProjectResource.setArchived({
                projectId: $scope.project.id
              }, {
                isArchived: true
              });
              $state.go('project', {
                userUid: $stateParams.userUid,
                projectId: newProjectId
              });
            };
          }
        }
      });
    };
    
    $scope.openCopyDialog = function() {
      $modal.open({
        templateUrl: './app/js/project/copyProject.html',
        scope: $scope,
        controller: 'CopyProjectController',
        resolve: {
          callback: function() {
            return function(newProjectId) {
              $state.go('project', {
                userUid: UserService.getLoginUser().id,
                projectId: newProjectId
              });
            };
          }
        }
      });
    };

    $scope.archiveAnalysis = function(analysis) {
      var params = {
        projectId: $scope.project.id,
        analysisId: analysis.id
      };
      AnalysisResource.setArchived(
        params, {
          isArchived: true
        }
      ).$promise.then(
        loadAnalyses);
    };

    $scope.unarchiveAnalysis = function(analysis) {
      var params = {
        projectId: $scope.project.id,
        analysisId: analysis.id
      };
      AnalysisResource.setArchived(
        params, {
          isArchived: false
        }
      ).$promise.then(
        loadAnalyses);
    };

    $scope.toggleShowArchived = function() {
      $scope.showArchived = !$scope.showArchived;
    };

  };
  return dependencies.concat(SingleProjectController);
});
