'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = ['$transitions', '$scope', '$stateParams', '$modal',
      'ProjectResource',
      'ReportResource',
      'DefaultReportService',
      'PageTitleService'
    ];
    var EditReportcontroller = function(
      $transitions,
      $scope,
      $stateParams,
      $modal,
      $timeout,
      ReportResource,
      DefaultReportService,
      PageTitleService
    ) {
      $scope.reportText = {
        text: '',
        changed: false
      };
      $scope.showSaving = false;
      $scope.showSaved = false;
      $scope.userId = $stateParams.userUid;
      $scope.saveChanges = saveChanges;
      $scope.resetToDefault = resetToDefault;
      $scope.insertTextAtCursor = insertTextAtCursor;
      $scope.openInsertDialog = openInsertDialog;

      ReportResource.get($stateParams).$promise.then(function(report) {
        $scope.reportText.text = report.data;
      });

      $transitions.onStart({}, function(transition) {
        if ($scope.reportText.changed) {
          var answer = confirm('There are unsaved changes, are you sure you want to leave this page?');
          if (!answer) {
            transition.abort();
          } else {
            $scope.reportText.changed = false;
          }
        }
      });

      PageTitleService.setPageTitle('EditReportController', 'Edit ' + $scope.project.name + '\'s report');

      function insertTextAtCursor(text) {
        var input = angular.element(document.querySelector('#report-input'));
        var cursorPos = input.prop('selectionStart');
        var textBefore = $scope.reportText.text.substring(0, cursorPos);
        var textAfter = $scope.reportText.text.substring(cursorPos);
        $scope.reportText.text = textBefore + text + textAfter;
        $scope.reportText.changed = true;
      }

      function saveChanges() {
        ReportResource.put($stateParams, $scope.reportText.text);
        $scope.showSaving = true;
        $scope.reportText.changed = false;
        $timeout(function() {
          $scope.showSaving = false;
          $scope.showSaved = true;
        }, 1000);
        $timeout(function() {
          $scope.showSaved = false;
        }, 3000);
      }

      function resetToDefault() {
        DefaultReportService.generateDefaultReport($stateParams.projectId).then(function(defaultReport) {
          $scope.reportText.text = defaultReport;
          $scope.reportText.changed = true;
        });
      }

      function openInsertDialog(directiveName) {
        $modal.open({
          templateUrl: './insertDirectiveDialog.html',
          controller: 'InsertDirectiveController',
          resolve: {
            callback: function() {
              return function(graphText) {
                insertTextAtCursor(graphText);
              };
            },
            directiveName: function() {
              return directiveName;
            }
          }
        });
      }
    };
    return dependencies.concat(EditReportcontroller);
  });
