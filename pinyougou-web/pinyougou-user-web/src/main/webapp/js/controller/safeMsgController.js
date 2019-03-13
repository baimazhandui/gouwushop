/** 定义控制器层 */
app.controller('safeMsgController', function ($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});


    $scope.savePassword = function () {
        if ($scope.entity.nickName && $scope.entity.password && $scope.confirmPassword) {
            if ($scope.entity.password == $scope.confirmPassword) {
                $scope.entity.id = $scope.user.id;
                baseService.sendPost("/user/savePassword", $scope.entity)
                    .then(function (response) {
                        if (response.data) {
                            alert("保存成功");
                            location.href = "/logout";
                        } else {
                            alert("保存失败");
                        }
                    })
            } else {
                alert("两次输入密码不一样!");
            }
        } else {
            alert("请输入完整!");
        }
    };

    $scope.checkCodeAndSmsCode = function (num) {
        if (num == 2) {
            if ($scope.newPhone) {
                $scope.user.phone = $scope.newPhone;
            } else {
                alert("请输入新手机号!");
                return;
            }
        }
        if ($scope.code && $scope.smsCode) {
            baseService.sendGet("/user/checkCodeAndSmsCode?phone=" + $scope.user.phone + "&code=" + $scope.code
                + "&smsCode=" + $scope.smsCode)
                .then(function (response) {
                    alert(response.data);
                    if (response.data == "验证成功!") {
                        if (num == 1) {
                            location.href = "home-setting-address-phone.html";
                        } else {
                            location.href = "home-setting-address-complete.html";
                        }
                    }
                })
        }
    };

    $scope.sendCode = function (num) {
        if (num == 2) {
            if ($scope.newPhone) {
                $scope.user.phone = $scope.newPhone;
            } else {
                alert("请输入新手机号!");
                return;
            }
        }
        baseService.sendGet("/user/sendCode?phone="
            + $scope.user.phone)
            .then(function (response) {
                alert(response.data ? "发送成功!" : "发送失败");
            });
    }
});