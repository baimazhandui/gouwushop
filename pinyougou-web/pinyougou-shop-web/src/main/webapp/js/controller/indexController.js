app.controller('indexController', function ($scope, baseService) {
    $scope.showLoginName = function () {
        baseService.sendGet("/showLoginName")
            .then(function (response) {
                // 获取响应数据
                $scope.loginName = response.data.loginName;
            })
    };
});