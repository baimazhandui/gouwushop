/** 定义控制器层 */
app.controller('userController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    /*$controller('baseController', {$scope: $scope});*/

    // 定义$scope.user
    $scope.user = {};

    $scope.save = function () {
        if ($scope.password != $scope.user.password) {
            alert("密码不一致,请重新输入");
            return;
        }
        baseService.sendPost("/user/save?smsCode="
            + $scope.smsCode, $scope.user)
            .then(function (response) {
                if (response.data) {
                    alert("注册成功");
                    $scope.user = {};
                    $scope.password = "";
                    $scope.smsCode = "";
                    location.href = "http://user.pinyougou.com";
                } else {
                    alert("注册失败");
                }

            })
    };

    $scope.sendCode = function () {
        if ($scope.user.phone) {
            baseService.sendGet("/user/sendCode?phone="
                + $scope.user.phone)
                .then(function (response) {
                    alert(response.data ? "发送成功!" : "发送失败");
                });
        } else {
            alert("请输入手机号码!");
        }
    };


});