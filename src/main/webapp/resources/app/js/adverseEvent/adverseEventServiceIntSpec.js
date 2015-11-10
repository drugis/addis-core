'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the adverse event service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch';

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var uuidServiceStub;
    var measurementMomentServiceMock;
    var adverseEventService;
    var studyService;
    var outcomeService;

    var addTemplateRaw;
    var addAdverseEventQueryRaw;
    var adverseEventsQuery;
    var deleteAdverseEventRaw;
    var editAdverseEventRaw;
    var queryAdverseEventMeasuredAtRaw;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        remotestoreServiceStub = jasmine.createSpyObj('RemoteRdfStoreService', [
          'create',
          'load',
          'executeUpdate',
          'executeQuery',
          'getGraph',
          'deFusekify'
        ]);
        $provide.value('RemoteRdfStoreService', remotestoreServiceStub);
      });
    });

    beforeEach(module('trialverse.outcome'));
    beforeEach(module('trialverse.adverseEvent'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', [
          'queryItems',
          'addItem',
          'editItem',
          'deleteIte ',
          'generateLabel'
        ]);
        $provide.value('MeasurementMomentService', measurementMomentServiceMock);
        uuidServiceStub = jasmine.createSpyObj('UUIDService', ['generate']);
        $provide.value('UUIDService', uuidServiceStub);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, AdverseEventService, StudyService, OutcomeService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      adverseEventService = AdverseEventService;
      studyService = StudyService;

      outcomeService = OutcomeService;
      spyOn(outcomeService, 'setOutcomeProperty');

      // reset the test graph
      testUtils.dropGraph(graphUri);

      // load study service templates
      testUtils.loadTemplate('createEmptyStudy.sparql', httpBackend);
      testUtils.loadTemplate('queryStudyData.sparql', httpBackend);

      // load service templates and flush httpBackend
      testUtils.loadTemplate('setOutcomeResultProperty.sparql', httpBackend);
      addTemplateRaw = testUtils.loadTemplate('addTemplate.sparql', httpBackend);
      addAdverseEventQueryRaw = testUtils.loadTemplate('addAdverseEvent.sparql', httpBackend);
      adverseEventsQuery = testUtils.loadTemplate('queryAdverseEvent.sparql', httpBackend);
      deleteAdverseEventRaw = testUtils.loadTemplate('deleteVariable.sparql', httpBackend);
      editAdverseEventRaw = testUtils.loadTemplate('editVariable.sparql', httpBackend);
      queryAdverseEventMeasuredAtRaw = testUtils.loadTemplate('queryMeasuredAt.sparql', httpBackend);

      httpBackend.flush();

      // create and load empty test store
      var createStoreDeferred = $q.defer();
      var createStorePromise = createStoreDeferred.promise;
      remotestoreServiceStub.create.and.returnValue(createStorePromise);

      var loadStoreDeferred = $q.defer();
      var loadStorePromise = loadStoreDeferred.promise;
      remotestoreServiceStub.load.and.returnValue(loadStorePromise);

      studyService.loadStore();
      createStoreDeferred.resolve(scratchStudyUri);
      loadStoreDeferred.resolve();
      rootScope.$digest();

      // stub remotestoreServiceStub.executeUpdate method
      remotestoreServiceStub.executeUpdate.and.callFake(function(uri, query) {
        query = query.replace(/\$graphUri/g, graphUri);

        // console.log('graphUri = ' + uri);
        // console.log('query = ' + query);

        testUtils.executeUpdateQuery(query);

        var executeUpdateDeferred = q.defer();
        executeUpdateDeferred.resolve();
        return executeUpdateDeferred.promise;
      });

    }));

    describe('addItem', function() {

      it('should add the adverseEvent triples and measuredAtMoments triples to the graph', function(done) {

        testUtils.loadTestGraph('emptyStudy.ttl', graphUri);

        // the test item to add
        var adverseEvent = {
          label: 'adverse event label',
          measurementType: 'http://some-measurementType'
        };
        var newUuid = 'newUuid';
        uuidServiceStub.generate.and.returnValue(newUuid);

        // add some measured at moments
        var moment1 = {uri: 'http://moments/moment1'};
        var moment2 = {uri: 'http://moments/moment2'};
        var adverseEventWithMoments = angular.copy(adverseEvent);
        adverseEventWithMoments.measuredAtMoments = [moment1, moment2];
        adverseEventWithMoments.uuid = newUuid;
        adverseEventWithMoments.uri = 'http://trials.drugis.org/instances/' + newUuid;

        var resultPromise = adverseEventService.addItem(adverseEventWithMoments);

        resultPromise.then(function(result) {
          // verify addAdverseEvent query
          var adverseEventsAsString = testUtils.queryTeststore(adverseEventsQuery.replace(/\$graphUri/g, graphUri));
          var adverseEventsObject = testUtils.deFusekify(adverseEventsAsString);
          expect(adverseEventsObject.length).toEqual(1);
          expect(adverseEventsObject[0].label).toEqual(adverseEvent.label);
          expect(adverseEventsObject[0].measurementType).toEqual(adverseEvent.measurementType);

          // verify outcome properties are added
          expect(outcomeService.setOutcomeProperty).toHaveBeenCalledWith(adverseEventWithMoments);

          // verify add measured at query
          var measuredAtQuery = queryAdverseEventMeasuredAtRaw.replace(/\$graphUri/g, graphUri);
          var adverseEventMeasuredAtAsString = testUtils.queryTeststore(measuredAtQuery);
          var measuredAtMoments = testUtils.deFusekify(adverseEventMeasuredAtAsString);
          expect(measuredAtMoments.length).toEqual(2);
          expect(measuredAtMoments[1].measurementMoment).toEqual('http://moments/moment2');
          expect(measuredAtMoments[0].measurementMoment).toEqual('http://moments/moment1');

          done();
        });

        rootScope.$digest();

      });
    });


  });
});
