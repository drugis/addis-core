<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ page session="false"%>

<!DOCTYPE html>
<html ng-app="trialverse">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width" />
  <link rel="shortcut icon" href="<c:url value="/app/img/favicon.ico" />" type="image/x-icon" />

  <title>trialverse.org</title>

  <link rel="stylesheet" type="text/css" href="app/js/bower_components/font-awesome/css/font-awesome.min.css">
  <link rel="stylesheet" type="text/css" href="<c:url value="/app/css/trialverse-drugis.css" />">
  <script src="app/js/bower_components/requirejs/require.js" data-main="app/js/main.js"></script>
</head>

<body class="f-topbar-fixed">
  <session-expired></session-expired>
  <div ui-view>
  </div>

  <script>
  window.config = {
    user : {
      name : "${account.firstName}",
      firstName : "${account.firstName}",
      lastName : "${account.lastName}",
      userEmail: "${userEmail}",
      userNameHash: "${userNameHash}"
    },
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
