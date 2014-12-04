'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource', '$location', '$anchorScroll', '$modal', 'StudyService'];
    var StudyController = function($scope, $stateParams, StudyResource, $location, $anchorScroll, $modal, StudyService) {

      $scope.study = {};
      $scope.arms = {};

      StudyResource.get($stateParams, function(responce, status) {
        StudyService.
        loadStore(responce.n3Data)
          .then(function(numberOfTriples) {
            console.log('loading study-store success, ' + numberOfTriples + ' triples loaded');

            StudyService.queryStudyData().then(function(studyQueryResult) {
              $scope.study = studyQueryResult;
            });

            StudyService.queryArmData().then(function(armsQueryResult) {
              $scope.arms = armsQueryResult;
            });
          }, function() {
            console.error('failed loading study-store');
          });
      });

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };

      function onArmCreation(arm) {
        console.log('arm created callback succes');
        StudyService.queryArmData().then(function(armsQueryResult) {
          $scope.arms = armsQueryResult;
        });
      }

      $scope.showArmDialog = function() {
        $modal.open({
          templateUrl: 'app/js/study/view/arm.html',
          scope: $scope,
          controller: 'ArmController',
          resolve: {
            successCallback: function() {
              return onArmCreation;
            }
          }
        });
      };
    };

    return dependencies.concat(StudyController);
  });