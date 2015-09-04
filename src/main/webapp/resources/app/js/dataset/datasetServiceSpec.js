'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var
      q,
      rootScope,
      remoteRdfStoreService,
      datasetService;

    beforeEach(module('trialverse.dataset', function($provide) {
      remoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', [
        'create', 'load', 'executeUpdate', 'executeQuery', 'getGraph'
      ]);
      $provide.value('RemoteRdfStoreService', remoteRdfStoreService);
    }));

    beforeEach(inject(function($rootScope, $q, DatasetService) {
      rootScope = $rootScope;
      q = $q;
      datasetService = DatasetService;
    }));

    describe('loadStore', function() {
      it('should create a remote store, then call the callback and load the data into the new remote store', function() {
        var
          called = false,
          calledWith,
          data = {},
          callback = function(arg) {
            called = true;
            calledWith = arg;
          },
          createDefer = q.defer();
        remoteRdfStoreService.create.and.returnValue(createDefer.promise);
        createDefer.resolve('graphURI');

        datasetService.loadStore(data, callback);

        rootScope.$apply();
        expect(remoteRdfStoreService.create).toHaveBeenCalled();
        expect(remoteRdfStoreService.load).toHaveBeenCalled();
        expect(called).toBeTruthy();
        expect(calledWith).toBe('graphURI');
      });
    });

    describe('executeQuery', function() {
      it('should call remoteRdfStoreService executeQuery', function() {
        datasetService.executeQuery('scratchUri', 'query');
        expect(remoteRdfStoreService.executeQuery).toHaveBeenCalled();
      });
    });
    describe('executeUpdate', function() {
      it('should call remoteRdfStoreService executeUpdate', function() {
        datasetService.executeUpdate('scratchUri', 'query');
        expect(remoteRdfStoreService.executeUpdate).toHaveBeenCalled();
      });
    });

  });
});
