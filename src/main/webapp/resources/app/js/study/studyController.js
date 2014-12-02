'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource', '$location', '$anchorScroll'];
    var StudyController = function($scope, $stateParams, StudyResource, $location, $anchorScroll) {

      StudyResource.get($stateParams).$promise.then(function(result) {
        var arms;
        var study;

        if (result['@graph']) {

          study = _.find(result['@graph'], function(item) {
            return item['@type'] === 'http://trials.drugis.org/ontology#Study';
          });

          arms = _.filter(result['@graph'], function(item) {
            return item['@type'] === 'http://trials.drugis.org/ontology#Arm';
          });
        } else {
          study = result;
        }

        study.arms = arms;
        $scope.study = study;

      });

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };
    };

    return dependencies.concat(StudyController);
  });