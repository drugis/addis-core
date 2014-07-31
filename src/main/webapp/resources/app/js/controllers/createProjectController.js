'use strict';
define([], function() {
	var dependencies = ['$scope', 'TrialverseResource'];
	var CreateProjectController = function($scope, TrialverseResource) {
    $scope.namespaces = TrialverseResource.query();
	};
	return dependencies.concat(CreateProjectController);
});
