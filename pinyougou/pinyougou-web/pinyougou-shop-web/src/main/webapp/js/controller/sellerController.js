/** 定义控制器层 */
app.controller('sellerController', function ($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    /** 添加 */
    $scope.saveOrUpdate = function () {
        /** 发送post请求 */
        baseService.sendPost("/seller/save", $scope.seller)
            .then(function (response) {
                if (response.data) {
                    /** 跳转到登录页面 */
                    location.href = "/shoplogin.html";
                } else {
                    alert("申请入驻失败！");
                }
            });
    };

    /** 查询登录商家信息 */
    $scope.findSeller = function () {
        baseService.sendGet("/seller/findSeller")
            .then(function (response) {
                $scope.seller = response.data;
            })
    };

    $scope.saveSellerMsg = function () {
        baseService.sendPost("/seller/saveSellerMsg", $scope.seller)
            .then(function (response) {
                if (response.data) {
                    $scope.findSeller();
                } else {
                    alert("操作失败");
                }
            })
    };

    $scope.changePassword = function () {
        if ($scope.oldPassword && $scope.newPassword && $scope.checkedPassword) {
            if ($scope.newPassword == $scope.checkedPassword) {
                baseService.sendPost("/seller/changePassword?oldPassword="
                    + $scope.oldPassword + "&newPassword=" + $scope.newPassword)
                    .then(function (response) {
                        alert(response.data);
                        if (response.data == "修改密码成功") {
                            location.href="/logout";
                        }
                    })
            } else {
                alert("两次输入密码不相同!");
            }
        } else {
            alert("请完整输入密码!");
        }
    };

    $scope.cleanData = function () {
        $scope.oldPassword = "";
        $scope.newPassword = "";
        $scope.checkedPassword = "";
    }
});
