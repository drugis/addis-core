<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ page session="false"%>

<!DOCTYPE html>
<html ng-app="addis">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width" />
    <link rel="shortcut icon" href="<c:url value="/app/img/favicon.ico" />" type="image/x-icon" />

    <title>addis.drugis.org</title>

    <link rel="stylesheet" type="text/css" href="app/js/bower_components/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="app/js/bower_components/jslider/dist/jquery.slider.min.css">
    <link rel="stylesheet" type="text/css" href="app/js/bower_components/nvd3/build/nv.d3.css">
    <link rel="stylesheet" type="text/css" href="<c:url value="/app/css/addis-drugis.css" />">
    <link rel="stylesheet" type="text/css" href="app/js/bower_components/angular-ui-select/dist/select.min.css">
    <link rel="stylesheet" type="text/css" href="app/js/bower_components/select2/select2.css">
    <!-- <link rel="stylesheet" type="text/css" href="app/js/bower_components/selectize/dist/css/selectize.default.css"> -->

    <script src="app/js/bower_components/requirejs/require.js" data-main="app/js/main.js"></script>
  </head>

  <body class="f-topbar-fixed">
    <session-expired-directive></session-expired-directive>

    <div ui-view></div>
    <script>

      if(window.location.host.indexOf("test") > -1) {
        document.body.className += " test";
        document.title = "test-" + document.title;
      }

      window.mcdaBasePath = 'app/js/bower_components/mcda-web/';
      window.config = {
        user : {
          id : ${account.id},
          name : "${account.firstName}",
          firstName : "${account.firstName}",
          lastName : "${account.lastName}",
          userEmail: "${userEmail}",
          userNameHash: "${userNameHash}"
        },
        WS_URI: "${pataviMcdaWsUri}",
        workspaceName: 'analyses',
        workspacesRepositoryUrl : "/projects/:projectId/analyses/:analysisId",
        _csrf_token : "${_csrf.token}",
        _csrf_header : "${_csrf.headerName}"
      };

      function signout(){
        var signoutForm = document.getElementById('signout_form');

        if(signoutForm){
          signoutForm.submit();
        }
      }
    </script>
    <div class="push"></div>

  </body>
</html>
