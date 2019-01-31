'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$q',
    '$stateParams',
    '$modal',
    '$injector',
    'ArmService',
    'GroupService',
    'MeasurementMomentService'
  ];
  var CategoryDirective = function(
    $q,
    $stateParams,
    $modal,
    $injector,
    ArmService,
    GroupService,
    MeasurementMomentService
  ) {
    return {
      restrict: 'E',
      templateUrl: './variableCategoryDirective.html',
      scope: {
        studyUuid: '=',
        settings: '=',
        isEditingAllowed: '=',
        isRepairable: '='
      },
      link: function(scope, element, attributes) {
        scope.reloadItems = reloadItems;
        scope.addItem = addItem;

        var service = $injector.get(scope.settings.service);

        scope.isSingleItem = !!attributes.isSingleItem;
        scope.reloadItems();

        var refreshStudyDesignLister;
        var deregisterRefreshListener;
        var variablesPromise, armsPromise, groupsPromise, measurementMomentsPromise;
        scope.showResults = false;

        // initialize the directive
        reloadResultTables();

        function reloadItems() {
          if (refreshStudyDesignLister) {
            // stop listning while loading
            refreshStudyDesignLister();
          }
          service.queryItems().then(function(queryResult) {
            scope.items = queryResult;
            refreshStudyDesignLister = scope.$on('refreshStudyDesign', function() {
              scope.reloadItems();
            });
          });
        }

        function reloadResultTables() {
          if (deregisterRefreshListener) {
            // stop listening while loading to prevent race conditions
            deregisterRefreshListener();
          }

          armsPromise = getArmsPromise();
          groupsPromise = getGroupsPromise();
          measurementMomentsPromise = getMeasurementMomentsPromise();
          variablesPromise = getVariablesPromise();

          $q.all([armsPromise, groupsPromise, variablesPromise]).then(setShowResults);
          $q.all([armsPromise, groupsPromise, measurementMomentsPromise, variablesPromise]).then(function() {
            scope.$broadcast('refreshResultsTable');
            registerListener();
          });
        }

        function setShowResults() {
          var isAnyMeasuredVariable = _.find(scope.variables, function(variable) {
            return variable.measuredAtMoments.length > 0;
          });
          scope.showResults = isAnyMeasuredVariable && (scope.arms.length > 0 || scope.groups.length > 0);
        }

        function getVariablesPromise() {
          return service.queryItems($stateParams.studyUUID).then(function(result) {
            scope.variables = result;
          });
        }

        function getMeasurementMomentsPromise() {
          return MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.measurementMoments = result;
            return result;
          });
        }

        function getGroupsPromise() {
          return GroupService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.groups = result;
            return result;
          });
        }

        function getArmsPromise() {
          return ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
            return result;
          });
        }

        function registerListener() {
          deregisterRefreshListener = scope.$on('refreshResults', function() {
            reloadResultTables();
          });
        }

        function onAdd() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        function addItem() {
          $modal.open({
            template: scope.settings.addItemtemplate,
            scope: scope,
            controller: scope.settings.addItemController,
            resolve: {
              callback: function() {
                return onAdd;
              },
              actionType: function() {
                return 'Add';
              },
              settings: function() {
                return scope.settings;
              }
            }
          });
        };
      }
    };
  };
  return dependencies.concat(CategoryDirective);
});
