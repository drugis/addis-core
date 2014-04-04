'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$q', '$window', '$location',
    'ProjectsService', 'AnalysisService', 'OutcomeService', 'InterventionService', 'Select2UtilService', 'TrialverseStudyService',
    'ProblemService', 'jQuery'
  ];
  var AnalysisController = function($scope, $stateParams, $q, $window, $location,
    ProjectsService, AnalysisService, OutcomeService, InterventionService,
    Select2UtilService, TrialverseStudyService, ProblemService, jQuery) {

    $scope.loading = {
      loaded: false
    };

    $scope.editMode = {
      disableEditing: true
    };

    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);

    $scope.outcomes = OutcomeService.query($stateParams);
    $scope.interventions = InterventionService.query($stateParams);

    $scope.selectedOutcomeIds = [];
    $scope.selectedInterventionIds = [];

    $q.all([
      $scope.project.$promise,
      $scope.analysis.$promise
    ]).then(function() {
      $scope.loading.loaded = true;

      var userIsOwner = $window.config.user.id === $scope.project.owner.id;
      $scope.editMode.disableEditing = !userIsOwner || $scope.analysis.problem;

      $scope.select2Options = {
        'readonly': $scope.editMode.disableEditing
      };

      //  angular ui bug work-around, select2-ui does not properly watch for changes in the select2-options 
      jQuery('#criteriaSelect').select2('readonly', $scope.editMode.disableEditing);
      jQuery('#interventionsSelect').select2('readonly', $scope.editMode.disableEditing);

      $scope.studies = TrialverseStudyService.query({
        id: $scope.project.trialverseId
      });
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);

      $scope.$watchCollection('selectedOutcomeIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
          $scope.analysis.$save();
        }
      });

      $scope.$watchCollection('selectedInterventionIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.analysis.selectedInterventions = Select2UtilService.idsToObjects($scope.selectedInterventionIds, $scope.interventions);
          $scope.analysis.$save();
        }
      });

      $scope.$watch('analysis.studyId', function(newValue, oldValue) {
        if (oldValue !== newValue) {
          $scope.analysis.$save();
        }
      });

      $scope.createProblem = function() {
        var analysis = $scope.analysis;
        analysis.problem = ProblemService.get($stateParams);
        var problem = analysis.problem;
        problem.$promise.then(function() {
          analysis.$save(function(analysis) {
            var newLocation = $location.url() + '/scenarios/' + analysis.scenarios[0].id;
            $location.url(newLocation);
          });
        });
      };
    });
  };
  return dependencies.concat(AnalysisController);
});