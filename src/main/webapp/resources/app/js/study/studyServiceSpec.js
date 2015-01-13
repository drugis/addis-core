'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('study service', function() {

    var remoteRdfStoreService, studyService,
      createDefer, loadDefer, executeUpdateDefer, executeQueryDefer,
      rootScope;

    beforeEach(module('trialverse', function($provide) {
      remoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', ['create', 'load', 'executeUpdate', 'executeQuery']);

      $provide.value('RemoteRdfStoreService', remoteRdfStoreService);
    }));

    describe('createEmptyStudy', function() {

      beforeEach(inject(function($rootScope, $q, StudyService) {
        rootScope = $rootScope;
        createDefer = $q.defer();
        loadDefer = $q.defer();
        executeUpdateDefer = $q.defer();
        executeQueryDefer = $q.defer();

        remoteRdfStoreService.create.and.returnValue(createDefer.promise);
        remoteRdfStoreService.load.and.returnValue(loadDefer.promise);
        remoteRdfStoreService.executeUpdate.and.returnValue(executeUpdateDefer.promise);
        remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
        studyService = StudyService;
      }));

      it('should be defined', function() {
        expect(studyService.createEmptyStudy).toBeDefined();
      });

      it('should return a graph of the new study', function() {
        var study = {
          label: 'label',
          comment: 'comment'
        };
        var newGraphUri = 'newUri';

        var promise = studyService.createEmptyStudy(study);

        createDefer.resolve(newGraphUri);
        executeUpdateDefer.resolve();

        rootScope.$digest();

        expect(remoteRdfStoreService.executeUpdate).toHaveBeenCalledWith(newGraphUri, jasmine.any(String));

        expect(promise.$$state.status).toBe(1);
      });
    });

    describe('reset', function() {
      it('should empty the store and reset the storeDefer', function() {
        studyService.reset();
        expect(studyService.isStudyModified()).toEqual(false)
      });
    });

    describe('after loading the store', function() {

      it('the load promise should be resolved', function() {
        var loadPromise = studyService.loadStore('data');
        createDefer.resolve();
        loadDefer.resolve();

        rootScope.$digest();

        expect(loadPromise.$$state.status).toBe(1);
      });

      describe('queryStudyData', function() {
        it('should request data from the remote store', function() {
          var data = {
            data: {
              results: {
                bindings: ['foo']
              }
            }
          };
          var expected = {
            $$state: {
              status: 1,
              value: 'foo'
            }
          };
          var studyData = studyService.queryStudyData();
          executeQueryDefer.resolve(data);
          rootScope.$digest();

          expect(studyData).toEqual(expected);

        });
      });

    });


  });
});
