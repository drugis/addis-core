'use strict';
define(['angular', 'lodash', 'jQuery'],
  function(angular, _, $) {
    var dependencies = ['$scope', '$stateParams', '$modal',
      'ProjectResource',
      'ReportResource',
      '$timeout'
    ];
    var EditReportcontroller = function($scope, $stateParams, $modal,
      ProjectResource,
      ReportResource,
      $timeout) {

      $scope.reportText = {
        text: ''
      };
      $scope.showSaving = false;
      $scope.showSaved = false;
      $scope.userId = $stateParams.userUid;
      $scope.saveChanges = saveChanges;
      $scope.resetToDefault = resetToDefault;
      $scope.insertTextAtCursor = insertTextAtCursor;
      $scope.openNetworkGraphDialog = openNetworkGraphDialog;
      $scope.openComparisonResultDialog = openComparisonResultDialog;
      $scope.openRelativeEffectsTableDialog = openRelativeEffectsTableDialog;
      $scope.openRelativeEffectsPlotDialog = openRelativeEffectsPlotDialog;
      $scope.openRankProbabilitiesTableDialog = openRankProbabilitiesTableDialog;
      $scope.openRankProbabilitiesPlotDialog = openRankProbabilitiesPlotDialog;

      ReportResource.get($stateParams).$promise.then(function(report) {
        $scope.reportText.text = report.data;
      });

      function insertTextAtCursor(text) {
        var input = $('#report-input');
        var cursorPos = input.prop('selectionStart');
        var textBefore = $scope.reportText.text.substring(0, cursorPos);
        var textAfter = $scope.reportText.text.substring(cursorPos);
        $scope.reportText.text = textBefore + text + textAfter;
      }

      function saveChanges() {
        ReportResource.put($stateParams, $scope.reportText.text);
        $scope.showSaving = true;
        $timeout(function() {
          $scope.showSaving = false;
          $scope.showSaved = true;
        }, 1000);
        $timeout(function() {
          $scope.showSaved = false;
        }, 3000);
      }

      function resetToDefault() {
        $scope.reportText.text = 'default report text';
      }

      function openNetworkGraphDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/networkGraph/insertNetworkGraphDialog.html',
          controller: 'InsertNetworkGraphController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }

      function openComparisonResultDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/comparisonResult/insertComparisonResultDialog.html',
          controller: 'InsertComparisonResultController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }

      function openRelativeEffectsTableDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/relativeEffectsTable/insertRelativeEffectsTableDialog.html',
          controller: 'InsertRelativeEffectsTableController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }

      function openRelativeEffectsPlotDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/relativeEffectsPlot/insertRelativeEffectsPlotDialog.html',
          controller: 'InsertRelativeEffectsPlotController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }

      function openRankProbabilitiesTableDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/rankProbabilitiesTable/insertRankProbabilitiesTableDialog.html',
          controller: 'InsertRankProbabilitiesTableController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }

      function openRankProbabilitiesPlotDialog() {
        $modal.open({
          templateUrl: './app/js/project/report/rankProbabilitiesPlot/insertRankProbabilitiesPlotDialog.html',
          controller: 'InsertRankProbabilitiesPlotController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            }
          }
        });
      }
    };
    return dependencies.concat(EditReportcontroller);
  });
