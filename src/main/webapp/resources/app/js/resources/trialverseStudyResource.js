'use strict';
define([], function () {
	var dependencies = ['$resource'];
	var TrialverseStudyResource = function ($resource) {
		return $resource('/namespaces/:id/studies', {
			id: '@id'
		});
	};
	return dependencies.concat(TrialverseStudyResource);
});