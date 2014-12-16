'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var uUIDService, rdfStoreService;

    beforeEach(module('trialverse.study'));

    describe('createEmptyStudy', function() {

      var studyService, q;

      beforeEach(function() {

        uUIDService = jasmine.createSpyObj('UUIDService', ['generate']);

        module('trialverse', function($provide) {
          $provide.value('UUIDService', uUIDService);
        });
      });

      beforeEach(inject(function($rootScope, $q, StudyService) {
        studyService = StudyService;
        q = $q;
      }));

      it('should be defined', function() {
        expect(studyService.createEmptyStudy).toBeDefined();
      });

      it('should return a graph of the new study', function() {
        var study = {
          label: 'studyLabel',
          comment: 'study comment'
        };
        var uuid = 'uuid';

        var promise = studyService.createEmptyStudy(uuid, study);

        expect(promise.$$state.value).toBeDefined();

      });

    });

    describe('resetStore', function() {

      var studyService, q;
      beforeEach(function() {

        uUIDService = jasmine.createSpyObj('UUIDService', ['generate']);
        rdfStoreService = jasmine.createSpyObj('RdfStoreService', ['create']);

        rdfStoreService.create.and.callFake(function(callback) {
          callback({
            mock: 'store',
            load: function(arg1, arg2, callback){
              callback(true, {mock: 'result'});
            }
          });
        });

        module('trialverse', function($provide) {
          $provide.value('UUIDService', uUIDService);
          $provide.value('RdfStoreService', rdfStoreService);
        });
      });

      beforeEach(inject(function($rootScope, $q, StudyService) {
        studyService = StudyService;
        q = $q;
        studyService.loadStore({
          mock: 'data'
        });
      }));

      it('should empty the store and reset the storeDefer', function() {
        studyService.resetStore();
      });
    });
  });
});