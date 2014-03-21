'use strict';
define([], function () {
	var dependencies = ['$resource'];
	var TrialverseStudyService = function ($resource) {
		return $resource('/namespaces/:id/studies', {
			id: '@id'
		});
	};
	return dependencies.concat(TrialverseStudyService);
});