'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('study service', function() {

    var remoteRdfStoreService, conceptService;

    beforeEach(module('trialverse', function($provide) {
      remoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', ['create', 'load', 'executeUpdate', 'executeQuery']);

      $provide.value('RemoteRdfStoreService', remoteRdfStoreService);
    }));
  });
});
