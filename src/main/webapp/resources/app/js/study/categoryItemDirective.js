'use strict';
define([], function() {
  var dependencies = ['$modal', '$injector'];

  var CategoryItemDirective = function($modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryItemDirective.html',
      scope: {
        arm: '=arm',
        callback: '=callback',
        mayCreate : '&mayCreate'
      },
      link: function(scope) {
        scope.editItem = function() {
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

  return dependencies.concat(CategoryItemDirective);
});
