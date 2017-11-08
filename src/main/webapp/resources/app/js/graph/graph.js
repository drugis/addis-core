'use strict';
var requires = [
'graph/graphResource',
'graph/versionedGraphResource'
];
define(requires.concat(['angular', 'angular-resource']),function (
	GraphResource, 
	VersionedGraphResource, 
	angular
	) {
  return angular.module('trialverse.graph', ['ngResource'])
    //resources
    .factory('GraphResource', GraphResource)
    .factory('VersionedGraphResource', VersionedGraphResource)
    ;
});
