'use strict';
define([], function() {
  var dependencies = ['$modal', 'ArmService'];

  var ArmDirective = function($modal, ArmService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/arm/arm.html',
      scope: {
        arm: '=arm',
        callback: '=callback'
      },
      link: function(scope) {

        scope.editArm = function() {
          $modal.open({
            templateUrl: 'app/js/arm/editArm.html',
            scope: scope,
            controller: 'EditArmController',
            resolve: {
              successCallback: function() {
                return function() {
                  console.log('its a success !');
                  scope.callback();
                };
              }
            }
          });
        };

        scope.deleteArm = function() {
          ArmService.deleteArm(scope.arm)
            .then(scope.callback);
        };
      }


    };
  };

  return dependencies.concat(ArmDirective);
});
