'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', '$stateParams', '$modal', '$injector', 'ArmService',
    'GroupService', 'MeasurementMomentService'];
  var CategoryDirective = function($q, $stateParams, $modal, $injector, ArmService, GroupService,
    MeasurementMomentService) {
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

        var refreshStudyDesignLister;
        var service = $injector.get(scope.settings.service);

        scope.isSingleItem = !!attributes.isSingleItem;

        scope.reloadItems = function() {
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
        };

        scope.reloadItems();

        var deregisterRefreshListener;
        var variablesPromise, armsPromise, groupsPromise, measurementMomentsPromise;
        scope.showResults = false;

        function reloadResultTables() {
          if(deregisterRefreshListener) {
            // stop listening while loading to prevent race conditions
            deregisterRefreshListener();
          }

          armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
            return result;
          });

          groupsPromise = GroupService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.groups = result;
            return result;
          });

          measurementMomentsPromise = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.measurementMoments = result;
            return result;
          });

          variablesPromise = service.queryItems($stateParams.studyUUID).then(function(result) {
            scope.variables = result;
          });

          $q.all([armsPromise, groupsPromise, variablesPromise]).then(function() {
            var isAnyMeasuredVariable = _.find(scope.variables, function(variable) {
              return variable.measuredAtMoments.length > 0;
            });
            scope.showResults = isAnyMeasuredVariable && (scope.arms.length > 0 || scope.groups.length > 0);
          });

          $q.all([armsPromise, groupsPromise, measurementMomentsPromise, variablesPromise]).then(function() {
            scope.$broadcast('refreshResultsTable');
            // register listnener as the loading is now done
            deregisterRefreshListener = scope.$on('refreshResults', function() {
              reloadResultTables();
            });
          });
        }

        // initialize the directive
        reloadResultTables();

        function onAdd() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        scope.addItem = function() {
          $modal.open({
            template: scope.settings.addItemTemplate,
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
