'use strict';
define([], function() {
  var dependencies = ['$modal'];

  var ArmDirective = function($modal) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/directives/arm/arm.html',
      scope: {
        arm: '=arm',
        callback: '=callback'
      },
      link: function(scope) {

        scope.editArm = function() {
          $modal.open({
            templateUrl: 'app/js/study/directives/arm/editArm.html',
            scope: scope,
            controller: 'EditArmController',
            resolve: {
              successCallback: function() {
                return function() {
                  console.log('its a success !');
                  scope.callback();
                }
              }
            }
          });
        };

        scope.deleteArm = function() {
          console.log('delete arm');
        };
      }


    };
  };

  return dependencies.concat(ArmDirective);
});