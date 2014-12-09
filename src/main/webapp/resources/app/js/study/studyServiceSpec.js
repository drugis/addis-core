'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var uUIDService;

    beforeEach(module('trialverse.study'));

    beforeEach(function() {
  
      uUIDService = jasmine.createSpyObj('UUIDService', ['generate']);

      module('trialverse', function($provide) {
    //    $provide.value('$q', $q);
        $provide.value('UUIDService', uUIDService);
      });
    });

    describe('createEmptyStudy', function() {
      
      var studyService;
      var q;
      var scope;

      beforeEach(inject(function($rootScope, $q, StudyService) {
        studyService = StudyService;
        q = $q;
        scope = $rootScope;
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
  });
});