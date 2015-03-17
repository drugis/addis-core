var should = require('should');
var assert = require('assert');
var request = require('supertest');

var trialverseUrl = process.env.TRIALVERSE_URL;
var sessionId = 'CAF0EED6DD71380322F91B7C4B7E5699';
var csrfToken = '1a230aa1-1311-43c4-b853-e28ee674d00c';

var newDataset = '{"title":"my-test-dataset","description":"my test  description"}';

describe('create dataset ', function() {

  it('should return a 201 ( created )', function(done) {
    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        if (err) {
          console.log('err =  ' + err);
          throw err;
        }
        console.log('res = ' + JSON.stringify(res));
        res.should.have.property('status', 201);
        done();
      });

  });
});

describe('get dataset', function() {
  var datasetUrl, datasetUuid;

  before(function(done) {
    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        datasetUrl = res.headers.location;
        datasetUuid = datasetUrl.split('/')[4];
        done();
      });
  });

  it('should get the dataset', function(done) {
    var expectedTtl =
      '<' + datasetUrl + '>\n' +
      '        a       <http://rdfs.org/ns/void#Dataset> ;\n' +
      '        <http://purl.org/dc/terms/description>\n' +
      '                "my test  description" ;\n' +
      '        <http://purl.org/dc/terms/title>\n' +
      '                "my-test-dataset" .\n';

    request(trialverseUrl)
      .get(/datasets/ + datasetUuid)
      .set('Accept', 'text/turtle')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .end(function(err, res) {
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });
});

describe('get study', function() {

  before(function(done) {

    var newStudy = '<http://trials.drugis.org/studies/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
      ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
      ' a  <http://trials.drugis.org/ontology#Study> ; ' +
      ' <http://trials.drugis.org/ontology#has_epochs> () . ';

    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        console.log(JSON.stringify(res));
        datasetUrl = res.headers.location;
        datasetUuid = datasetUrl.split('/')[4];
        request(trialverseUrl + '/datasets/' + datasetUuid)
          .put('/studies/myStudyUuid')
          .set('Content-Type', 'text/turtle')
          .set('Cookie', 'JSESSIONID=' + sessionId)
          .set('X-CSRF-TOKEN', csrfToken)
          .send(newStudy)
          .end(done);
      });
  });

  it('should get the study', function(done) {
    var expectedTtl = 'nonsense';
    request(trialverseUrl)
      .get('/datasets/' + datasetUuid + '/studies/myStudyUuid')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .set('Accept', 'text/turtle')
      .end(function(err, res) {
        console.log('++++Get study responcay+++++++\n\n\n' + JSON.stringify(res));
        res.should.have.property('status', 200);
        res.headers['content-type'].should.equal('text/turtle;charset=UTF-8');
        res.text.should.equal(expectedTtl);
        done();
      });
  });

});


describe('query studies with details', function() {

  var datasetUrl;

  before(function(done) {

    var newStudy = '<http://trials.drugis.org/studies/studyUuid>  <http://www.w3.org/2000/01/rdf-schema#label> "mystudy" ;' +
      ' <http://www.w3.org/2000/01/rdf-schema#comment> "myComment" ;' +
      ' a  <http://trials.drugis.org/ontology#Study> ; ' +
      ' <http://trials.drugis.org/ontology#has_epochs> () . ';

    request(trialverseUrl)
      .post('/datasets')
      .set('Content-Type', 'application/json;charset=UTF-8')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('X-CSRF-TOKEN', csrfToken)
      .send(newDataset)
      .end(function(err, res) {
        console.log(JSON.stringify(res));
        datasetUrl = res.headers.location;
        datasetUuid = datasetUrl.split('/')[4];
        request(trialverseUrl + '/datasets/' + datasetUuid)
          .put('/studies/myStudyUuid')
          .set('Content-Type', 'text/turtle')
          .set('Cookie', 'JSESSIONID=' + sessionId)
          .set('X-CSRF-TOKEN', csrfToken)
          .send(newStudy)
          .end(done);
      });
  });

  it('should return query result', function(done) {
    console.log("start the test");
    request(trialverseUrl)
      .get('/datasets/' + datasetUuid + '/studiesWithDetail')
      .set('Cookie', 'JSESSIONID=' + sessionId)
      .set('Accept', 'application/json;charset=UTF-8')
      .end(function(err, res) {
        res.should.have.property('status', 200);
        console.log('+++++++++++++++++' + JSON.stringify(res));
        JSON.parse(res.text).results.bindings[0].label.value.should.equal('mystudy');
        JSON.parse(res.text).results.bindings[0].title.value.should.equal('myComment');
        done();
      });
  });
});



//
//   node-debug -p 8030 _mocha jena-api-test.js 
//