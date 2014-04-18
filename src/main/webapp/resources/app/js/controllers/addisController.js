'use strict';
define([], function() {
    var dependencies = ['$scope', '$window'];
    var AddisController = function($scope, $window) {
        $scope.user = $window.config.user;
        $scope.showBreadcrumbs = false;
        $scope.breadcrumbs = [];
    };
    return dependencies.concat(AddisController);
});
